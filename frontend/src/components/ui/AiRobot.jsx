import React from "react";

export function AiRobot({ size = 72, className = "" }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      style={{ filter: "drop-shadow(0 8px 16px rgba(108, 93, 211, 0.35))" }}
    >
      <defs>
        {/* Gradients */}
        <linearGradient id="bodyGrad" x1="20" y1="20" x2="80" y2="90" gradientUnits="userSpaceOnUse">
          <stop stopColor="#9D8CFF" />
          <stop offset="0.5" stopColor="#6C5DD3" />
          <stop offset="1" stopColor="#4A3CA8" />
        </linearGradient>
        <linearGradient id="faceGrad" x1="25" y1="35" x2="75" y2="75" gradientUnits="userSpaceOnUse">
          <stop stopColor="#1B1736" />
          <stop offset="1" stopColor="#0B0918" />
        </linearGradient>
        <linearGradient id="eyeGrad" x1="0" y1="0" x2="1" y2="1">
          <stop stopColor="#67E8F9" />
          <stop offset="1" stopColor="#06B6D4" />
        </linearGradient>
        <radialGradient id="antennaGlow" cx="50%" cy="50%" r="50%">
          <stop stopColor="#C4B5FD" />
          <stop offset="1" stopColor="#8B5CF6" />
        </radialGradient>
      </defs>

      {/* Antenna */}
      <circle cx="50" cy="14" r="5" fill="url(#antennaGlow)" />
      <rect x="48" y="18" width="4" height="10" rx="2" fill="#7C3AED" />

      {/* Robot Ears / Side nodes */}
      <rect x="14" y="44" width="7" height="16" rx="3.5" fill="#5848C2" />
      <rect x="79" y="44" width="7" height="16" rx="3.5" fill="#5848C2" />
      <circle cx="17.5" cy="52" r="2" fill="#A78BFA" />
      <circle cx="82.5" cy="52" r="2" fill="#A78BFA" />

      {/* Main Robot Head */}
      <rect x="18" y="26" width="64" height="52" rx="20" fill="url(#bodyGrad)" />

      {/* Head Highlight */}
      <ellipse cx="50" cy="31" rx="20" ry="3" fill="rgba(255, 255, 255, 0.4)" />

      {/* Face Screen */}
      <rect x="25" y="36" width="50" height="34" rx="14" fill="url(#faceGrad)" stroke="#372F66" strokeWidth="1.5" />

      {/* Glowing Cyan Eyes */}
      <circle cx="39" cy="50" r="5.5" fill="url(#eyeGrad)" />
      <circle cx="37.5" cy="48.5" r="2" fill="#FFFFFF" />

      <circle cx="61" cy="50" r="5.5" fill="url(#eyeGrad)" />
      <circle cx="59.5" cy="48.5" r="2" fill="#FFFFFF" />

      {/* Happy Smile */}
      <path
        d="M 43 60 Q 50 65 57 60"
        stroke="#67E8F9"
        strokeWidth="2.2"
        strokeLinecap="round"
        fill="none"
      />

      {/* Little Rosy Cheeks */}
      <circle cx="33" cy="56" r="2.5" fill="#EC4899" opacity="0.6" />
      <circle cx="67" cy="56" r="2.5" fill="#EC4899" opacity="0.6" />

      {/* Subtle Robot Torso Hint */}
      <path
        d="M 32 80 Q 50 85 68 80 L 72 94 Q 50 97 28 94 Z"
        fill="#5B4DC2"
        opacity="0.9"
      />
      <circle cx="50" cy="88" r="2.5" fill="#67E8F9" />
    </svg>
  );
}

export default AiRobot;
