import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { meeting as meetingApi, chat as chatApi, recordings as recApi } from '../api';

const ICE_SERVERS = [
  { urls: 'stun:stun.l.google.com:19302' },
  { urls: 'stun:stun1.l.google.com:19302' },
  { urls: 'stun:stun2.l.google.com:19302' },
  { urls: 'stun:stun3.l.google.com:19302' },
  // TURN servers for NAT traversal (when STUN alone fails)
  { urls: 'turn:relay1.expressturn.com:3478', username: 'efBURNSQGW3DNHYD', credential: 'j07STleDNSHOzUkj' },
  { urls: 'turn:a.relay.metered.ca:80', username: 'b0930ef2afd81e2c4e1a3b6a', credential: 'mhHMnhOzp9CKONBS' },
  { urls: 'turn:a.relay.metered.ca:443', username: 'b0930ef2afd81e2c4e1a3b6a', credential: 'mhHMnhOzp9CKONBS' },
  { urls: 'turn:a.relay.metered.ca:443?transport=tcp', username: 'b0930ef2afd81e2c4e1a3b6a', credential: 'mhHMnhOzp9CKONBS' },
];

/* ── Page shell – OUTSIDE component so it's a stable reference ── */
function PageShell({ children, onBack }) {
  return (
    <div style={{
      minHeight: '100vh', display: 'flex', flexDirection: 'column',
      background: 'linear-gradient(145deg,#0f0f1a 0%,#1a1a2e 60%,#0d0d1a 100%)',
      fontFamily: "'Poppins',sans-serif", color: '#e8e8e8',
    }}>
      <div style={{
        padding: '12px 24px', background: 'rgba(0,0,0,0.5)',
        borderBottom: '1px solid rgba(255,107,0,0.3)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span style={{ fontSize: 22 }}>📹</span>
          <span style={{
            fontFamily: 'Orbitron,sans-serif', fontSize: 18, fontWeight: 900,
            background: 'linear-gradient(135deg,#FF6B00,#CC5500)',
            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
          }}>MTNG</span>
          <span style={{ fontSize: 12, color: '#666' }}>Meeting Platform</span>
        </div>
        {onBack && (
          <button onClick={onBack} style={{
            background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.15)',
            color: '#bbb', padding: '6px 14px', borderRadius: 6, cursor: 'pointer', fontSize: 13,
          }}>← Dashboard</button>
        )}
      </div>
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
        {children}
      </div>
    </div>
  );
}

export default function MeetingRoomPage() {
  const { user, isAdmin } = useAuth();
  const navigate = useNavigate();
  const { roomName: paramRoomName } = useParams();

  // Page states: 'loading' | 'error' | 'prejoin' | 'mic-denied' | 'joining' | 'active'
  const [pageState, setPageState] = useState('loading');
  const [errorMsg, setErrorMsg] = useState('');
  const [renderError, setRenderError] = useState(null);

  // Meeting data
  const [meetingData, setMeetingData] = useState(null);

  // Active meeting state
  const [micOn, setMicOn] = useState(true);
  const [listenOnly, setListenOnly] = useState(false);
  const [recording, setRecording] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [participants, setParticipants] = useState([]);
  const [chatMsgs, setChatMsgs] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [sidebarTab, setSidebarTab] = useState('chat');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [micStatus, setMicStatus] = useState('unknown'); // 'unknown'|'granted'|'denied'|'unavailable'
  const [allRooms, setAllRooms] = useState([]);
  const [showRoomSwitcher, setShowRoomSwitcher] = useState(false);

  // Refs – never go stale
  const stompRef = useRef(null);
  const localStream = useRef(null);
  const peers = useRef({});
  const remoteAudios = useRef({});
  const recorder = useRef(null);
  const recordedChunks = useRef([]);
  const timerRef = useRef(null);
  const chatBottomRef = useRef(null);
  const recStartTime = useRef(null);
  const currentRoom = useRef('');
  const meetingDataRef = useRef(null);
  const userRef = useRef(null);
  const listenOnlyRef = useRef(false);
  const recordingRef = useRef(false);
  const participantNames = useRef({}); // NEW: Track display names by peerId
  const audioContainerRef = useRef(null); // DOM container for audio elements
  const autoRecordStarted = useRef(false); // Track if auto-recording was started

  // Keep refs in sync with latest values
  useEffect(() => { meetingDataRef.current = meetingData; }, [meetingData]);
  useEffect(() => { userRef.current = user; }, [user]);
  useEffect(() => { listenOnlyRef.current = listenOnly; }, [listenOnly]);
  useEffect(() => { recordingRef.current = recording; }, [recording]);

  // ── Load meeting data ─────────────────────────────────────────────────────
  useEffect(() => {
    (async () => {
      try {
        let m = null;
        if (paramRoomName) m = await meetingApi.getRoom(paramRoomName).catch(() => null);
        if (!m) m = await meetingApi.active().catch(() => null);
        if (!m) { setErrorMsg('No active meeting room found. It may have ended.'); setPageState('error'); return; }
        setMeetingData(m);
        currentRoom.current = m.roomName;
        meetingDataRef.current = m;
        // Check mic permission before showing pre-join
        checkMicPermission();
        setPageState('prejoin');
      } catch (e) {
        setErrorMsg('Failed to load meeting: ' + (e.message || 'Unknown'));
        setPageState('error');
      }
    })();
  }, [paramRoomName]); // eslint-disable-line react-hooks/exhaustive-deps

  const checkMicPermission = async () => {
    try {
      if (!navigator.mediaDevices?.getUserMedia) { setMicStatus('unavailable'); return; }
      if (navigator.permissions) {
        const r = await navigator.permissions.query({ name: 'microphone' }).catch(() => null);
        if (r) {
          const map = s => s === 'granted' ? 'granted' : s === 'denied' ? 'denied' : 'unknown';
          setMicStatus(map(r.state));
          r.onchange = () => setMicStatus(map(r.state));
          return;
        }
      }
      setMicStatus('unknown');
    } catch { setMicStatus('unknown'); }
  };

  // ── Timer ─────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (pageState !== 'active') return;
    timerRef.current = setInterval(() => setElapsed(e => e + 1), 1000);
    return () => clearInterval(timerRef.current);
  }, [pageState]);

  // ── Auto-start recording when meeting becomes active ─────────────────────
  useEffect(() => {
    if (pageState !== 'active') return;
    if (autoRecordStarted.current) return;
    // Delay to allow WebRTC connections to establish first
    const timer = setTimeout(() => {
      if (!recordingRef.current && !autoRecordStarted.current) {
        console.log('🎙️ Auto-starting recording...');
        autoRecordStarted.current = true;
        // Start recording (will pick up local + remote streams)
        try { toggleRecording(); } catch (e) { console.warn('Auto-record start failed:', e); }
      }
    }, 4000); // 4 second delay
    return () => clearTimeout(timer);
  }, [pageState]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => { chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [chatMsgs]);

  useEffect(() => {
    if (pageState !== 'active' || !isAdmin) return;
    const fetch = () => meetingApi.rooms().then(r => setAllRooms(r || [])).catch(() => {});
    fetch();
    const id = setInterval(fetch, 8000);
    return () => clearInterval(id);
  }, [pageState, isAdmin]);

  const fmtTime = s => {
    const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60), sec = s % 60;
    return h > 0 ? `${h}:${String(m).padStart(2,'0')}:${String(sec).padStart(2,'0')}` : `${m}:${String(sec).padStart(2,'0')}`;
  };

  // ── WebRTC helpers (all use refs – never stale) ───────────────────────────
  const sendSignal = (data) => {
    if (!stompRef.current?.connected || !currentRoom.current) {
      console.warn('❌ Cannot send signal - STOMP not connected or no room');
      return;
    }
    try {
      console.log('📤 Sending signal:', data.type, 'from:', userRef.current?.username, 'displayName:', data.displayName);
      stompRef.current.send(`/app/signal/${currentRoom.current}`, {}, JSON.stringify(data));
    }
    catch (e) { console.warn('sendSignal error:', e); }
  };

  const removePeer = (peerId) => {
    const pc = peers.current[peerId];
    if (pc) { try { pc.close(); } catch {} delete peers.current[peerId]; }
    const a = remoteAudios.current[peerId];
    if (a) {
      a.srcObject = null;
      try { a.parentNode?.removeChild(a); } catch {} // Remove from DOM
      delete remoteAudios.current[peerId];
    }
    setParticipants(prev => prev.filter(p => p.id !== peerId));
  };

  const createPeer = (peerId) => {
    if (peers.current[peerId]) { try { peers.current[peerId].close(); } catch {} delete peers.current[peerId]; }
    const pc = new RTCPeerConnection({ iceServers: ICE_SERVERS });

    pc.onicecandidate = e => {
      if (e.candidate) sendSignal({ type: 'ice-candidate', from: userRef.current?.username, to: peerId, data: JSON.stringify(e.candidate) });
    };

    pc.ontrack = e => {
      console.log('🔊 ontrack event received from', peerId, 'streams:', e.streams?.length, 'track kind:', e.track?.kind);
      let audio = remoteAudios.current[peerId];
      if (!audio) {
        audio = document.createElement('audio');
        audio.autoplay = true;
        audio.playsInline = true;
        audio.controls = false;
        audio.id = `remote-audio-${peerId}`;
        audio.setAttribute('data-peer', peerId);
        // Append to DOM - critical for browser autoplay policy
        const container = audioContainerRef.current || document.body;
        container.appendChild(audio);
        remoteAudios.current[peerId] = audio;
        console.log('✓ Created and appended audio element to DOM for', peerId);
      }
      if (e.streams?.[0]) {
        console.log('✓ Setting audio srcObject for', peerId, 'tracks:', e.streams[0].getAudioTracks().length);
        audio.srcObject = e.streams[0];
        // Ensure volume is up
        audio.volume = 1.0;
        audio.muted = false;
        // Play with retry logic
        const tryPlay = (attempt = 0) => {
          audio.play().then(() => {
            console.log('✅ Audio playing successfully for', peerId);
          }).catch(err => {
            console.warn(`Audio play attempt ${attempt + 1} failed for ${peerId}:`, err.message);
            if (attempt < 3) {
              setTimeout(() => tryPlay(attempt + 1), 500 * (attempt + 1));
            }
          });
        };
        tryPlay();
      } else if (e.track) {
        // Fallback: create stream from individual track
        console.log('⚠ No stream in ontrack, creating from track for', peerId);
        const stream = new MediaStream([e.track]);
        audio.srcObject = stream;
        audio.volume = 1.0;
        audio.muted = false;
        audio.play().catch(err => console.warn('Fallback audio play error:', err));
      }
    };

    pc.onconnectionstatechange = () => {
      console.log('Connection state for', peerId, ':', pc.connectionState);
      if (['disconnected','failed','closed'].includes(pc.connectionState)) removePeer(peerId);
    };
    pc.oniceconnectionstatechange = () => {
      console.log('ICE connection state for', peerId, ':', pc.iceConnectionState);
      if (pc.iceConnectionState === 'failed') pc.restartIce?.();
    };

    // Add local audio tracks if available
    if (localStream.current?.getTracks().length > 0) {
      console.log('Adding', localStream.current.getTracks().length, 'local tracks to peer', peerId);
      localStream.current.getTracks().forEach(t => {
        // Ensure track is enabled before adding
        if (t.kind === 'audio') t.enabled = true;
        console.log('Adding track:', t.kind, 'enabled:', t.enabled, 'readyState:', t.readyState);
        pc.addTrack(t, localStream.current);
      });
    } else {
      console.log('No local stream, adding audio transceiver in recvonly mode for', peerId);
      try { pc.addTransceiver('audio', { direction: 'recvonly' }); } catch (e) { console.warn('addTransceiver error:', e); }
    }

    peers.current[peerId] = pc;
    return pc;
  };

const createPeerAndOffer = async (peerId) => {
  try {
    const pc = createPeer(peerId);
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    sendSignal({
      type: 'offer',
      from: userRef.current?.username,
      displayName: userRef.current?.displayName || userRef.current?.username,
      to: peerId,
      data: JSON.stringify(pc.localDescription)
    });
  } catch (e) { console.error('offer error:', e); }
};

  const handleOffer = async (peerId, offer, displayName) => {
    try {
      // Use provided displayName or retrieve from stored names or fallback to peerId
      const participantName = displayName || participantNames.current[peerId] || peerId;
      setParticipants(prev => {
        const exists = prev.find(p => p?.id === peerId);
        if (exists) return prev;
        return [...prev, { id: peerId, name: participantName, isLocal: false, micOn: true }];
      });
      // Try to get mic if we don't have it yet and not in listen-only mode
      if (!localStream.current && !listenOnlyRef.current && navigator.mediaDevices?.getUserMedia) {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
          localStream.current = stream;
          setMicOn(true); setMicStatus('granted');
        } catch {}
      }
      const pc = createPeer(peerId);
      await pc.setRemoteDescription(new RTCSessionDescription(offer));
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      sendSignal({
        type: 'answer',
        from: userRef.current?.username,
        displayName: userRef.current?.displayName || userRef.current?.username,
        to: peerId,
        data: JSON.stringify(pc.localDescription)
      });
    } catch (e) { console.error('handleOffer error:', e); }
  };

  const handleAnswer = async (peerId, answer) => {
    try {
      const pc = peers.current[peerId];
      if (pc && pc.signalingState !== 'stable') await pc.setRemoteDescription(new RTCSessionDescription(answer));
    } catch (e) { console.error('handleAnswer error:', e); }
  };

  const handleCandidate = async (peerId, candidate) => {
    try {
      const pc = peers.current[peerId];
      if (pc?.remoteDescription) await pc.addIceCandidate(new RTCIceCandidate(candidate));
    } catch {}
  };

  // ── Signal handler (plain function using refs – zero stale closure risk) ──
  const handleSignal = (data) => {
    const me = userRef.current?.username;
    if (!me || !data) return;

    // Backend uses 'from', support both for compatibility
    const sender = data.from || data.sender;
    if (!sender || sender === me) return;

    console.log('📡 Signal received:', data.type, 'from:', sender, 'displayName:', data.displayName);

    try {
      switch (data.type) {
        case 'join':
          // Store display name from backend
          if (data.displayName) participantNames.current[sender] = data.displayName;
          setParticipants(prev => {
            const exists = prev.find(p => p?.id === sender);
            if (exists) return prev;
            console.log('✓ Adding participant:', sender, 'as:', data.displayName);
            return [...prev, {
              id: sender || 'unknown',
              name: data.displayName || sender || 'Guest',
              isLocal: false,
              micOn: true
            }];
          });
          if (sender) createPeerAndOffer(sender);
          break;
        case 'offer':
          if (sender && data.data) {
            if (data.displayName) participantNames.current[sender] = data.displayName;
            try {
              const offer = typeof data.data === 'string' ? JSON.parse(data.data) : data.data;
              console.log('✓ Received offer from:', sender);
              handleOffer(sender, offer, data.displayName);
            } catch (e) {
              console.error('Failed to parse offer data:', e);
            }
          }
          break;
        case 'answer':
          if (sender && data.data) {
            try {
              const answer = typeof data.data === 'string' ? JSON.parse(data.data) : data.data;
              console.log('✓ Received answer from:', sender);
              handleAnswer(sender, answer);
            } catch (e) {
              console.error('Failed to parse answer data:', e);
            }
          }
          break;
        case 'ice-candidate':
          if (sender && data.data) {
            try {
              const candidate = typeof data.data === 'string' ? JSON.parse(data.data) : data.data;
              console.log('✓ Received ICE candidate from:', sender);
              handleCandidate(sender, candidate);
            } catch (e) {
              console.error('Failed to parse candidate data:', e);
            }
          }
          break;
        case 'leave':
          if (sender) {
            console.log('✓ Participant left:', sender);
            delete participantNames.current[sender];
            removePeer(sender);
          }
          break;
        case 'mic-toggle':
          setParticipants(prev => prev.map(p => p?.id === sender ? { ...p, micOn: data.micOn === true || data.micOn === false ? data.micOn : true } : p));
          break;
        case 'chat':
          if (data.data) setChatMsgs(prev => [...prev, { sender: data.displayName || sender || 'Anonymous', content: data.data, time: new Date().toLocaleTimeString() }]);
          break;
        case 'room-ended':
          doCleanup();
          navigate('/');
          break;
        default:
          console.log('Unknown signal type:', data.type);
          break;
      }
    } catch (err) {
      console.error('handleSignal error:', err, data);
    }
  };

  // ── Cleanup ───────────────────────────────────────────────────────────────
  const doCleanup = () => {
    if (recordingRef.current && recorder.current?.state !== 'inactive') try { recorder.current.stop(); } catch {}
    sendSignal({ type: 'leave', from: userRef.current?.username });
    Object.keys(peers.current).forEach(id => removePeer(id));
    // Clean up all audio DOM elements
    Object.values(remoteAudios.current).forEach(a => {
      try { a.srcObject = null; a.parentNode?.removeChild(a); } catch {}
    });
    remoteAudios.current = {};
    if (localStream.current) { localStream.current.getTracks().forEach(t => t.stop()); localStream.current = null; }
    try { stompRef.current?.disconnect(); } catch {}
    clearInterval(timerRef.current);
    autoRecordStarted.current = false;
  };

  useEffect(() => () => {
    if (localStream.current) localStream.current.getTracks().forEach(t => t.stop());
    Object.values(peers.current).forEach(pc => { try { pc.close(); } catch {} });
    try { stompRef.current?.disconnect(); } catch {}
    clearInterval(timerRef.current);
  }, []);

  // ── Join Meeting ──────────────────────────────────────────────────────────
  const joinMeeting = async (forceListenOnly = false) => {
    const m = meetingDataRef.current;
    if (!m) return;
    setPageState('joining');

    let isListenOnly = forceListenOnly;

    if (!isListenOnly) {
      if (!navigator.mediaDevices?.getUserMedia) {
        setMicStatus('unavailable');
        isListenOnly = true;
      } else {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
          localStream.current = stream;
          setMicOn(true);
          setMicStatus('granted');
        } catch (err) {
          if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError' || err.name === 'NotFoundError') {
            setMicStatus('denied');
            setPageState('mic-denied');
            return;
          }
          isListenOnly = true;
          console.warn('Mic error, switching to listen-only:', err.message);
        }
      }
    }

    listenOnlyRef.current = isListenOnly;
    setListenOnly(isListenOnly);
    if (isListenOnly) { localStream.current = null; setMicOn(false); }

    try {
      await meetingApi.join(m.roomName);
      if (!window.SockJS || !window.Stomp) throw new Error('WebSocket libraries not loaded – please reload.');
      const socket = new window.SockJS(`${window.location.protocol}//${window.location.host}/ws`);
      const stomp = window.Stomp.over(socket);
      stomp.debug = null;
      stomp.connect({}, () => {
        stompRef.current = stomp;
        stomp.subscribe(`/topic/room/${m.roomName}`, msg => {
          try { handleSignal(JSON.parse(msg.body)); } catch {}
        });
        sendSignal({
          type: 'join',
          from: userRef.current?.username,
          displayName: userRef.current?.displayName || userRef.current?.username
        });
        setElapsed(0);
        setParticipants([{ id: userRef.current?.username, name: userRef.current?.displayName || userRef.current?.username, isLocal: true, micOn: !isListenOnly }]);
        setPageState('active');
      }, err => {
        console.error('STOMP error:', err);
        setErrorMsg('WebSocket connection failed. Please refresh and try again.');
        setPageState('error');
      });
    } catch (err) {
      setErrorMsg('Failed to join: ' + (err.message || 'Unknown error'));
      setPageState('error');
    }
  };

  // ── Meeting controls ──────────────────────────────────────────────────────
  const toggleMic = () => {
    if (!localStream.current) return;
    const t = localStream.current.getAudioTracks()[0];
    if (t) { t.enabled = !t.enabled; setMicOn(t.enabled); sendSignal({ type: 'mic-toggle', from: userRef.current?.username, micOn: t.enabled }); }
  };

  const toggleRecording = () => {
    if (recording) {
      recorder.current?.stop();
      setRecording(false);
      recordingRef.current = false;
      return;
    }
    // Can record if we have local mic OR remote audio streams
    const hasRemoteAudio = Object.values(remoteAudios.current).some(a => a?.srcObject);
    if (!localStream.current && !hasRemoteAudio) {
      console.warn('No audio sources available for recording yet, will retry...');
      // Retry after a delay if auto-recording
      if (autoRecordStarted.current) {
        setTimeout(() => {
          if (!recordingRef.current) {
            console.log('Retrying auto-recording...');
            try { toggleRecording(); } catch {}
          }
        }, 3000);
      }
      return;
    }
    recordedChunks.current = [];
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const dest = audioCtx.createMediaStreamDestination();
      let sourcesAdded = 0;
      // Add local mic if available
      if (localStream.current) {
        try {
          audioCtx.createMediaStreamSource(localStream.current).connect(dest);
          sourcesAdded++;
          console.log('✓ Added local mic to recording');
        } catch (e) { console.warn('Could not add local stream to recording:', e); }
      }
      // Add all remote audio streams
      Object.entries(remoteAudios.current).forEach(([pid, a]) => {
        try {
          if (a?.srcObject) {
            audioCtx.createMediaStreamSource(a.srcObject).connect(dest);
            sourcesAdded++;
            console.log('✓ Added remote audio from', pid, 'to recording');
          }
        } catch (e) { console.warn('Could not add remote stream from', pid, ':', e); }
      });
      // If no real sources, add a silent oscillator so MediaRecorder has something
      if (sourcesAdded === 0) {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        gain.gain.value = 0; // silent
        osc.connect(gain);
        gain.connect(dest);
        osc.start();
        console.log('⚠ No audio sources, added silent track for recording');
      }
      console.log(`Recording with ${sourcesAdded} audio source(s)`);
      const mime = MediaRecorder.isTypeSupported('audio/webm;codecs=opus') ? 'audio/webm;codecs=opus' : 'audio/webm';
      const mr = new MediaRecorder(dest.stream, { mimeType: mime });
      mr.ondataavailable = e => {
        if (e.data.size > 0) recordedChunks.current.push(e.data);
      };
      mr.onstop = async () => {
        const blob = new Blob(recordedChunks.current, { type: 'audio/webm' });
        console.log('Recording stopped. Blob size:', blob.size);
        const dur = recStartTime.current ? Math.round((Date.now() - recStartTime.current) / 1000) : 0;

        // Auto-save to server
        if (blob.size > 0) {
          try {
            console.log('Saving recording to server...');
            const saved = await recApi.saveSession({
              meetingId: meetingDataRef.current?.id,
              participantName: userRef.current?.displayName || userRef.current?.username,
              durationSeconds: dur,
              fileName: `recording_${Date.now()}.webm`,
              fileSize: blob.size
            });
            if (saved?.id) {
              console.log('Recording session saved with ID:', saved.id);
              const fd = new FormData();
              fd.append('audio', blob, 'recording.webm');
              const uploadRes = await fetch(`/api/recordings/${saved.id}/upload-audio`, {
                method: 'POST',
                body: fd,
                credentials: 'same-origin'
              });
              if (uploadRes.ok) {
                console.log('✓ Recording uploaded successfully');
              } else {
                console.error('Upload failed:', uploadRes.status);
              }
            }
          } catch (e) {
            console.error('Save recording error:', e);
          }
        }

        // Always download to user's computer
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `mtng_recording_${Date.now()}.webm`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        try { audioCtx.close(); } catch {}
      };
      mr.start(1000);
      recorder.current = mr;
      recStartTime.current = Date.now();
      setRecording(true);
      recordingRef.current = true;
      console.log('Recording started with mime type:', mime);
    } catch (err) {
      console.error('Recording start failed:', err);
    }
  };

  const sendChat = e => {
    e.preventDefault();
    if (!chatInput.trim()) return;
    const u = userRef.current;
    setChatMsgs(prev => [...prev, { sender: u?.displayName || u?.username, content: chatInput.trim(), time: new Date().toLocaleTimeString() }]);
    sendSignal({ type: 'chat', from: u?.username, displayName: u?.displayName || u?.username, data: chatInput.trim() });
    chatApi.send(chatInput.trim()).catch(() => {});
    setChatInput('');
  };

  const leaveMeeting = async () => { doCleanup(); try { await meetingApi.leave(currentRoom.current); } catch {} navigate('/'); };
  const endMeeting = async () => { doCleanup(); try { await meetingApi.leave(currentRoom.current); await meetingApi.stop(currentRoom.current); } catch {} navigate('/'); };
  const switchRoom = async rn => {
    if (rn === currentRoom.current) { setShowRoomSwitcher(false); return; }
    doCleanup(); try { await meetingApi.leave(currentRoom.current); } catch {}
    setPageState('loading'); setParticipants([]); setChatMsgs([]); setElapsed(0);
    setMicOn(true); setRecording(false); setListenOnly(false); setShowRoomSwitcher(false);
    navigate(`/meeting-room/${rn}`, { replace: true });
  };

  // ── Renderers ─────────────────────────────────────────────────────────────

  // Catch render errors
  if (renderError) return (
    <PageShell onBack={() => navigate('/')}>
      <div style={{ background: '#1e1e2e', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 20, padding: '40px 32px', textAlign: 'center', maxWidth: 460, width: '95vw', display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'center' }}>
        <span style={{ fontSize: 56 }}>❌</span>
        <h2 style={{ color: '#e8e8e8', fontSize: 20 }}>Rendering Error</h2>
        <p style={{ color: '#888', fontSize: 13 }}>{renderError}</p>
        <button className="btn btn-primary" onClick={() => { setRenderError(null); window.location.reload(); }}>🔄 Reload Page</button>
      </div>
    </PageShell>
  );

  if (pageState === 'loading') return (
    <PageShell>
      <div style={{ textAlign: 'center' }}>
        <div className="loading-spinner" style={{ margin: '0 auto 16px' }} />
        <p style={{ color: '#888' }}>Loading meeting room…</p>
      </div>
    </PageShell>
  );

  if (pageState === 'error') return (
    <PageShell onBack={() => navigate('/')}>
      <div style={{ background: '#1e1e2e', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 20, padding: '40px 32px', textAlign: 'center', maxWidth: 460, width: '95vw', display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'center' }}>
        <span style={{ fontSize: 56 }}>📭</span>
        <h2 style={{ color: '#e8e8e8', fontSize: 20 }}>{errorMsg || 'Meeting not found'}</h2>
        <p style={{ color: '#888', fontSize: 13 }}>The meeting may have ended or the link is invalid.</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>← Back to Dashboard</button>
      </div>
    </PageShell>
  );

  if (pageState === 'mic-denied') return (
    <PageShell onBack={() => navigate('/')}>
      <div style={{ background: '#1e1e2e', border: '1px solid rgba(102,126,234,0.4)', borderRadius: 20, padding: '40px 32px', textAlign: 'center', maxWidth: 480, width: '95vw', display: 'flex', flexDirection: 'column', gap: 14, alignItems: 'center' }}>
        <span style={{ fontSize: 56 }}>🎤🚫</span>
        <h2 style={{ color: '#e8e8e8' }}>Microphone Access Denied</h2>
        <div style={{ background: 'rgba(255,107,0,0.08)', border: '1px solid rgba(255,107,0,0.25)', borderRadius: 10, padding: '12px 16px', textAlign: 'left', fontSize: 13, lineHeight: 2, width: '100%' }}>
          <strong style={{ color: '#FF9944' }}>Chrome / Edge:</strong> Click 🔒 in address bar → Microphone → Allow → Reload<br />
          <strong style={{ color: '#FF9944' }}>Firefox:</strong> Click 🎤 in address bar → Allow microphone → Reload<br />
          <strong style={{ color: '#FF9944' }}>Safari:</strong> Settings → Websites → Microphone → Allow
        </div>
        <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => window.location.reload()}>🔄 Retry with Microphone</button>
        <button className="btn btn-success" style={{ width: '100%' }} onClick={() => joinMeeting(true)}>👂 Join as Listener (No Mic)</button>
        <button className="btn btn-outline" onClick={() => navigate('/')}>← Back</button>
      </div>
    </PageShell>
  );

  if (pageState === 'joining') return (
    <PageShell>
      <div style={{ textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20 }}>
        <div className="loading-spinner" style={{ margin: '0 auto' }} />
        <h2 style={{ color: '#e8e8e8', fontSize: 22 }}>Connecting to meeting…</h2>
        <p style={{ color: '#888', fontSize: 14 }}>Setting up audio and WebSocket connection</p>
      </div>
    </PageShell>
  );

  if (pageState === 'prejoin') return (
    <PageShell onBack={() => navigate('/')}>
      <div style={{ background: '#1e1e2e', border: '1px solid rgba(102,126,234,0.4)', borderRadius: 20, padding: '40px 32px', textAlign: 'center', maxWidth: 460, width: '95vw', display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'center' }}>
        <h2 style={{ fontSize: 22, fontWeight: 700, background: 'linear-gradient(135deg,#667eea,#764ba2)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          🎙️ Ready to Join?
        </h2>
        <div style={{ background: 'rgba(255,255,255,0.04)', borderRadius: 10, padding: '12px 20px', width: '100%' }}>
          <p style={{ fontSize: 16, fontWeight: 700, color: '#e8e8e8', marginBottom: 4 }}>{meetingData?.title}</p>
          <p style={{ fontSize: 12, color: '#666' }}>Room: <code style={{ color: '#93c5fd' }}>{meetingData?.roomName}</code></p>
          {meetingData?.createdBy && <p style={{ fontSize: 12, color: '#888' }}>Host: {meetingData.createdBy}</p>}
        </div>
        {meetingData?.invitedParticipants && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, justifyContent: 'center' }}>
            {(Array.isArray(meetingData.invitedParticipants)
              ? meetingData.invitedParticipants
              : typeof meetingData.invitedParticipants === 'string'
                ? meetingData.invitedParticipants.split(',').filter(Boolean)
                : Object.keys(meetingData.invitedParticipants || {})
            ).filter(Boolean).map(u => (
              <span key={u} style={{ background: 'rgba(102,126,234,0.15)', color: '#93c5fd', border: '1px solid rgba(102,126,234,0.3)', borderRadius: 12, padding: '2px 10px', fontSize: 12 }}>{u}</span>
            ))}
          </div>
        )}
        {/* Mic status */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8, padding: '10px 16px', borderRadius: 8, width: '100%', justifyContent: 'center',
          background: micStatus === 'granted' ? 'rgba(34,197,94,0.1)' : micStatus === 'denied' ? 'rgba(239,68,68,0.1)' : 'rgba(255,255,255,0.05)',
          border: `1px solid ${micStatus === 'granted' ? 'rgba(34,197,94,0.3)' : micStatus === 'denied' ? 'rgba(239,68,68,0.3)' : 'rgba(255,255,255,0.1)'}`,
        }}>
          <span style={{ fontSize: 16 }}>{micStatus === 'granted' ? '🟢' : micStatus === 'denied' ? '🔴' : '⏳'}</span>
          <span style={{ fontSize: 13, color: micStatus === 'granted' ? '#86efac' : micStatus === 'denied' ? '#fca5a5' : '#aaa' }}>
            {micStatus === 'granted' ? 'Microphone ready' : micStatus === 'denied' ? 'Mic blocked – will join as listener' : 'Checking microphone…'}
          </span>
        </div>
        {/* User info */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 44, height: 44, borderRadius: '50%', background: 'linear-gradient(135deg,#667eea,#764ba2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 18 }}>
            {(user?.displayName || user?.username || '?')[0].toUpperCase()}
          </div>
          <span style={{ color: '#e8e8e8' }}>{user?.displayName || user?.username}</span>
          {isAdmin && <span style={{ fontSize: 11, background: 'rgba(255,107,0,0.2)', color: '#FF9944', padding: '2px 8px', borderRadius: 10, border: '1px solid rgba(255,107,0,0.3)' }}>ADMIN</span>}
        </div>
        <button onClick={() => joinMeeting(false)} style={{ width: '100%', padding: '14px', fontSize: 16, fontWeight: 700, background: 'linear-gradient(135deg,#22C55E,#16a34a)', border: 'none', borderRadius: 12, color: '#fff', cursor: 'pointer', letterSpacing: 0.5 }}>
          🎙️ Join Meeting
        </button>
        <button onClick={() => joinMeeting(true)} style={{ width: '100%', padding: '11px', fontSize: 14, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.15)', borderRadius: 10, color: '#bbb', cursor: 'pointer' }}>
          👂 Join as Listener (No Mic)
        </button>
        <button onClick={() => navigate('/')} style={{ background: 'none', border: 'none', color: '#666', cursor: 'pointer', fontSize: 13 }}>← Back to Dashboard</button>
      </div>
    </PageShell>
  );

  // ── Active Meeting View ───────────────────────────────────────────────────
  try {
    return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', background: '#0d0d1a', overflow: 'hidden', fontFamily: "'Poppins',sans-serif", color: '#e8e8e8' }}>
      {/* Hidden container for remote audio elements - MUST be in DOM for autoplay */}
      <div ref={audioContainerRef} style={{ position: 'absolute', width: 0, height: 0, overflow: 'hidden', pointerEvents: 'none' }} />
      {/* Header */}
      <div style={{ background: 'linear-gradient(135deg,#1e1e2e,#2d1b69)', padding: '10px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(102,126,234,0.3)', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <span style={{ fontSize: 15, fontWeight: 700 }}>🎙️ {meetingData?.title}</span>
          <span style={{ fontFamily: 'Courier New,monospace', fontSize: 14, background: 'rgba(0,0,0,0.3)', padding: '4px 12px', borderRadius: 16, color: '#A5F3FC', letterSpacing: 1 }}>{fmtTime(elapsed)}</span>
          <span style={{ background: 'rgba(0,0,0,0.25)', padding: '6px 12px', borderRadius: 6, fontSize: 12, color: '#ddd' }}>👥 {participants.length}</span>
          {recording && <span style={{ color: '#EF4444', fontSize: 12, background: 'rgba(0,0,0,0.25)', padding: '6px 12px', borderRadius: 6 }}>🔴 REC</span>}
          {listenOnly && <span style={{ color: '#a5f3fc', fontSize: 12, background: 'rgba(102,126,234,0.2)', padding: '6px 12px', borderRadius: 6 }}>👂 Listen Only</span>}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {isAdmin && allRooms.length > 1 && (
            <button onClick={() => setShowRoomSwitcher(v => !v)} style={{ background: 'rgba(102,126,234,0.2)', color: '#93c5fd', border: '1px solid rgba(102,126,234,0.4)', padding: '6px 14px', borderRadius: 6, fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
              🔀 Rooms ({allRooms.length})
            </button>
          )}
          <button onClick={leaveMeeting} style={{ background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.2)', color: '#ccc', padding: '6px 14px', borderRadius: 6, fontSize: 12, cursor: 'pointer' }}>📞 Leave</button>
          {isAdmin && <button onClick={endMeeting} style={{ background: '#EF4444', color: '#fff', border: 'none', padding: '8px 20px', borderRadius: 6, fontWeight: 700, fontSize: 13, cursor: 'pointer' }}>⏹️ End Room</button>}
        </div>
      </div>

      {/* Room Switcher */}
      {showRoomSwitcher && isAdmin && (
        <div style={{ background: 'linear-gradient(135deg,#1a1a2e,#2d2d3e)', borderBottom: '1px solid rgba(102,126,234,0.3)', padding: '12px 16px' }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#93c5fd', marginBottom: 8 }}>🔀 Switch Room</div>
          {allRooms.map(room => (
            <div key={room.id} onClick={() => switchRoom(room.roomName)} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', background: room.roomName === currentRoom.current ? 'rgba(34,197,94,0.08)' : 'rgba(0,0,0,0.2)', borderRadius: 8, marginBottom: 6, cursor: 'pointer', border: `1px solid ${room.roomName === currentRoom.current ? '#22C55E' : 'transparent'}` }}>
              <div>
                <div style={{ fontSize: 13, fontWeight: 600 }}>{room.title}</div>
                <div style={{ fontSize: 11, color: '#888' }}>👥 {room.inMeetingCount || 0} · {room.roomName}</div>
              </div>
              <span style={{ fontSize: 10, fontWeight: 700, padding: '3px 10px', borderRadius: 12, background: room.roomName === currentRoom.current ? 'rgba(34,197,94,0.15)' : 'rgba(102,126,234,0.15)', color: room.roomName === currentRoom.current ? '#22C55E' : '#93c5fd' }}>
                {room.roomName === currentRoom.current ? 'Current' : 'Switch →'}
              </span>
            </div>
          ))}
          <button onClick={() => setShowRoomSwitcher(false)} style={{ width: '100%', marginTop: 8, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#aaa', padding: '6px', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}>Close</button>
        </div>
      )}

      {/* Content */}
      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        {/* Audio Grid + Controls */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(160px,1fr))', gap: 10, padding: 16, overflowY: 'auto', alignContent: 'start', background: '#0a0a14' }}>
            {participants.length === 0 ? (
              <div style={{ gridColumn: '1 / -1', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 60, color: '#555', gap: 12 }}>
                <span style={{ fontSize: 56 }}>🎙️</span>
                <p style={{ fontSize: 15, color: '#666' }}>You are connected!</p>
                <p style={{ fontSize: 13, color: '#555' }}>Waiting for others to join…</p>
              </div>
            ) : participants.map(p => {
              const displayName = p?.name || p?.id || 'Guest';
              const displayInitial = (displayName || '?')[0].toUpperCase();
              return (
              <div key={p?.id || Math.random()} style={{ background: '#1e1e2e', borderRadius: 10, padding: '20px 14px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, border: `1px solid ${p?.isLocal ? 'rgba(102,126,234,0.4)' : 'rgba(255,255,255,0.07)'}`, opacity: p?.micOn ? 1 : 0.7, position: 'relative' }}>
                <div style={{ width: 56, height: 56, borderRadius: '50%', background: 'linear-gradient(135deg,#667eea,#764ba2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 22 }}>
                  {displayInitial}
                </div>
                <div style={{ fontSize: 12, fontWeight: 600, textAlign: 'center' }}>{displayName}{p?.isLocal ? ' (You)' : ''}</div>
                <div style={{ fontSize: 16 }}>{p?.micOn ? '🎤' : '🔇'}</div>
                {p?.micOn && <div style={{ position: 'absolute', top: 8, right: 8, width: 8, height: 8, borderRadius: '50%', background: '#22C55E', boxShadow: '0 0 6px rgba(34,197,94,0.6)' }} />}
              </div>
            );
            })}
          </div>

          {/* Controls */}
          <div style={{ background: '#000', padding: '12px 20px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 16, borderTop: '1px solid rgba(255,255,255,0.1)', flexShrink: 0 }}>
            {[
              { icon: listenOnly ? '🔇' : micOn ? '🎤' : '🔇', label: listenOnly ? 'No Mic' : micOn ? 'Mic On' : 'Mic Off', action: listenOnly ? null : toggleMic, active: !listenOnly && micOn, danger: !micOn },
              { icon: recording ? '⏹️' : '⏺️', label: recording ? 'Stop Rec' : 'Record', action: toggleRecording, danger: recording },
              { icon: '💬', label: 'Chat', action: () => setSidebarOpen(v => !v) },
              { icon: '📞', label: 'Leave', action: leaveMeeting, danger: true, end: true },
              ...(isAdmin ? [{ icon: '⏹️', label: 'End Room', action: endMeeting, danger: true }] : []),
            ].map((c, i) => (
              <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button onClick={c.action || undefined} disabled={!c.action} style={{
                  width: c.end ? 58 : 52, height: c.end ? 58 : 52, borderRadius: '50%',
                  background: c.danger ? '#EF4444' : c.active ? '#22C55E' : 'rgba(255,255,255,0.15)',
                  border: `1px solid ${c.danger ? '#EF4444' : c.active ? '#22C55E' : 'rgba(255,255,255,0.25)'}`,
                  color: '#fff', cursor: c.action ? 'pointer' : 'not-allowed', fontSize: c.end ? 22 : 20,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  opacity: !c.action ? 0.45 : 1, transition: 'all 0.2s',
                }}>
                  {c.icon}
                </button>
                <span style={{ fontSize: 10, color: 'rgba(255,255,255,0.5)', marginTop: 4 }}>{c.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Sidebar */}
        {sidebarOpen && (
          <div style={{ width: 320, background: '#1e1e1e', display: 'flex', flexDirection: 'column', borderLeft: '1px solid #333', flexShrink: 0 }}>
            <div style={{ background: 'linear-gradient(135deg,#667eea,#764ba2)', padding: '12px 14px', display: 'flex', gap: 8, flexShrink: 0 }}>
              {['chat','people'].map(tab => (
                <button key={tab} onClick={() => setSidebarTab(tab)} style={{ padding: '6px 12px', borderRadius: 5, cursor: 'pointer', background: sidebarTab === tab ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.2)', border: sidebarTab === tab ? '1px solid #00d4ff' : '1px solid transparent', color: '#fff', fontWeight: 600, fontSize: 12 }}>
                  {tab === 'chat' ? `💬 Chat` : `👥 People (${participants.length})`}
                </button>
              ))}
            </div>
            <div style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
              {sidebarTab === 'chat' ? (
                <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                  <div style={{ flex: 1, overflowY: 'auto', padding: 10 }}>
                    {chatMsgs.length === 0 && <div style={{ textAlign: 'center', padding: 30, fontSize: 13, color: '#555' }}>No messages yet 💬</div>}
                    {chatMsgs.map((m, i) => {
                      const isOwn = m.sender === (userRef.current?.displayName || userRef.current?.username);
                      return (
                        <div key={i} style={{ display: 'flex', marginBottom: 8, justifyContent: isOwn ? 'flex-end' : 'flex-start' }}>
                          <div style={{ maxWidth: '80%', padding: '8px 12px', borderRadius: 12, background: isOwn ? '#FF6B00' : '#2d2d3e', wordBreak: 'break-word', borderBottomRightRadius: isOwn ? 4 : 12, borderBottomLeftRadius: isOwn ? 12 : 4 }}>
                            <div style={{ fontSize: 10, opacity: 0.7, marginBottom: 2, fontWeight: 700 }}>{m.sender}</div>
                            <div style={{ fontSize: 13 }}>{m.content || m.message}</div>
                            <div style={{ fontSize: 10, opacity: 0.55, textAlign: 'right', marginTop: 3 }}>{m.time}</div>
                          </div>
                        </div>
                      );
                    })}
                    <div ref={chatBottomRef} />
                  </div>
                  <form onSubmit={sendChat} style={{ display: 'flex', gap: 8, padding: 10, borderTop: '1px solid #333', flexShrink: 0 }}>
                    <input value={chatInput} onChange={e => setChatInput(e.target.value)} placeholder="Type a message…" style={{ flex: 1, background: '#2d2d2d', border: '1px solid #444', color: '#fff', padding: '9px 12px', borderRadius: 20, fontSize: 13, outline: 'none' }} />
                    <button type="submit" style={{ width: 38, height: 38, borderRadius: '50%', background: '#FF6B00', color: '#fff', border: 'none', cursor: 'pointer', fontSize: 16, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>➤</button>
                  </form>
                </div>
              ) : (
                <div style={{ padding: 10, overflowY: 'auto' }}>
                  {participants.map(p => {
                    const displayName = p?.name || p?.id || 'Guest';
                    const displayInitial = (displayName || '?')[0].toUpperCase();
                    return (
                    <div key={p?.id || Math.random()} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', background: '#2a2a2a', borderRadius: 8, marginBottom: 8, border: '1px solid #333' }}>
                      <div style={{ width: 36, height: 36, borderRadius: '50%', background: 'linear-gradient(135deg,#667eea,#764ba2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: '#fff', flexShrink: 0 }}>{displayInitial}</div>
                      <div>
                        <div style={{ fontSize: 13, fontWeight: 600 }}>{displayName}{p?.isLocal ? ' (You)' : ''}</div>
                        <div style={{ fontSize: 11, color: p?.micOn ? '#22C55E' : '#888' }}>{p?.micOn ? '🎤 Speaking' : '🔇 Muted'}</div>
                      </div>
                    </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
    );
  } catch (err) {
    console.error('MeetingRoomPage render error:', err);
    setRenderError(err.message || 'Unknown rendering error');
    return (
      <PageShell onBack={() => navigate('/')}>
        <div style={{ background: '#1e1e2e', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 20, padding: '40px 32px', textAlign: 'center', maxWidth: 460, width: '95vw', display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'center' }}>
          <span style={{ fontSize: 56 }}>❌</span>
          <h2 style={{ color: '#e8e8e8', fontSize: 20 }}>Rendering Error</h2>
          <p style={{ color: '#888', fontSize: 13 }}>{err.message || 'Unknown error'}</p>
          <button className="btn btn-primary" onClick={() => window.location.reload()}>🔄 Reload Page</button>
        </div>
      </PageShell>
    );
  }
}


