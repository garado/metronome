const fs = require("fs");
const path = require("path");

const sampleRate = 44100;

function writeWav(outPath, samples) {
    const dataSize = samples.length * 2;
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
    for (let i = 0; i < samples.length; i++) {
        buffer.writeInt16LE(samples[i], 44 + i * 2);
    }
    fs.mkdirSync(path.dirname(outPath), { recursive: true });
    fs.writeFileSync(outPath, buffer);
}

function generateClick(frequency, decay, amplitude) {
    const numSamples = Math.floor(sampleRate * 0.05);
    const samples = new Int16Array(numSamples);
    for (let i = 0; i < numSamples; i++) {
        const t = i / sampleRate;
        const envelope = Math.exp(-t * decay);
        const signal = Math.sin(2 * Math.PI * frequency * t);
        samples[i] = Math.floor(envelope * signal * amplitude);
    }
    return samples;
}

// Regular click: 1kHz
writeWav(
    path.join(__dirname, "../assets/sounds/click.wav"),
    generateClick(1000, 120, 32767)
);
console.log("Generated click.wav");

// Accent click: higher pitch (1500Hz), slightly longer decay
writeWav(
    path.join(__dirname, "../assets/sounds/click-accent.wav"),
    generateClick(1500, 80, 32767)
);
console.log("Generated click-accent.wav");
