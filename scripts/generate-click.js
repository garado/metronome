const fs = require("fs");
const path = require("path");

// Generate a short click sound: 1kHz tone with fast exponential decay
const sampleRate = 44100;
const duration = 0.05; // 50ms
const numSamples = Math.floor(sampleRate * duration);

const samples = new Int16Array(numSamples);
for (let i = 0; i < numSamples; i++) {
    const t = i / sampleRate;
    const envelope = Math.exp(-t * 120);
    const signal = Math.sin(2 * Math.PI * 1000 * t);
    samples[i] = Math.floor(envelope * signal * 32767);
}

// Write WAV file
const dataSize = numSamples * 2;
const buffer = Buffer.alloc(44 + dataSize);
buffer.write("RIFF", 0);
buffer.writeUInt32LE(36 + dataSize, 4);
buffer.write("WAVE", 8);
buffer.write("fmt ", 12);
buffer.writeUInt32LE(16, 16);
buffer.writeUInt16LE(1, 20); // PCM
buffer.writeUInt16LE(1, 22); // mono
buffer.writeUInt32LE(sampleRate, 24);
buffer.writeUInt32LE(sampleRate * 2, 28);
buffer.writeUInt16LE(2, 32);
buffer.writeUInt16LE(16, 34);
buffer.write("data", 36);
buffer.writeUInt32LE(dataSize, 40);
for (let i = 0; i < numSamples; i++) {
    buffer.writeInt16LE(samples[i], 44 + i * 2);
}

const outPath = path.join(__dirname, "../assets/sounds/click.wav");
fs.mkdirSync(path.dirname(outPath), { recursive: true });
fs.writeFileSync(outPath, buffer);
console.log("Generated assets/sounds/click.wav");
