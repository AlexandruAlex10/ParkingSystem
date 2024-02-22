/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      backgroundImage: {
        'background': "url('src/assets/images/background.jpg')",
      }
    },
    colors: {
      c1: '#FFFFFF',
      c2: '#000000',
      c3: '#32B4CC',
      c4: '#C9CC32',
      c5: '#9BCC32',
      c6: '#CC4432',
      c7: '#1E1E1E',
    }
  },
  plugins: [],
}
