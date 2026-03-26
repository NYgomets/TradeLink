/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"DM Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      colors: {
        bg: {
          primary: '#0a0c10',
          secondary: '#111318',
          card: '#161a22',
          hover: '#1c2130',
        },
        border: {
          subtle: '#1e2433',
          DEFAULT: '#252d3d',
        },
        accent: {
          cyan: '#00d4ff',
          green: '#00e676',
          red: '#ff4d6d',
          amber: '#ffb300',
        },
        text: {
          primary: '#e8eaf0',
          secondary: '#7b8499',
          muted: '#4a5168',
        },
      },
      animation: {
        'fade-up': 'fadeUp 0.4s ease forwards',
        'pulse-subtle': 'pulseSubtle 2s ease-in-out infinite',
        'slide-in': 'slideIn 0.3s ease forwards',
      },
      keyframes: {
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        pulseSubtle: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.6' },
        },
        slideIn: {
          '0%': { opacity: '0', transform: 'translateX(-8px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
      },
    },
  },
  plugins: [],
}
