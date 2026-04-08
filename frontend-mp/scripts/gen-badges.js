/**
 * 生成勋章占位图 (纯 Node.js，无需 canvas 依赖)
 * 生成简单的 1x1 彩色 PNG 占位符
 * 实际项目中应替换为设计师提供的勋章图标
 */
const fs = require('fs');
const path = require('path');

// 最小有效 PNG: 1x1 像素，不同颜色
// 使用 zlib deflate 压缩的 IDAT chunk
function createPng(r, g, b) {
  // PNG signature
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // IHDR chunk: 32x32, 8-bit RGBA
  const width = 32, height = 32;
  const ihdrData = Buffer.alloc(13);
  ihdrData.writeUInt32BE(width, 0);
  ihdrData.writeUInt32BE(height, 4);
  ihdrData[8] = 8; // bit depth
  ihdrData[9] = 2; // color type: RGB
  ihdrData[10] = 0; // compression
  ihdrData[11] = 0; // filter
  ihdrData[12] = 0; // interlace
  const ihdr = makeChunk('IHDR', ihdrData);

  // IDAT: raw image data (filter byte 0 + RGB for each row)
  const rawRow = Buffer.alloc(1 + width * 3);
  rawRow[0] = 0; // filter: none
  for (let x = 0; x < width; x++) {
    rawRow[1 + x * 3] = r;
    rawRow[2 + x * 3] = g;
    rawRow[3 + x * 3] = b;
  }
  const rawData = Buffer.alloc(rawRow.length * height);
  for (let y = 0; y < height; y++) rawRow.copy(rawData, y * rawRow.length);

  const zlib = require('zlib');
  const compressed = zlib.deflateSync(rawData);
  const idat = makeChunk('IDAT', compressed);

  // IEND
  const iend = makeChunk('IEND', Buffer.alloc(0));

  return Buffer.concat([sig, ihdr, idat, iend]);
}

function makeChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeB = Buffer.from(type, 'ascii');
  const crcData = Buffer.concat([typeB, data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(crcData), 0);
  return Buffer.concat([len, typeB, data, crc]);
}

function crc32(buf) {
  let c = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) {
    c ^= buf[i];
    for (let j = 0; j < 8; j++) {
      c = (c >>> 1) ^ (c & 1 ? 0xEDB88320 : 0);
    }
  }
  return (c ^ 0xFFFFFFFF) >>> 0;
}

const badges = [
  { level: 1, color: [144, 238, 144] }, // 浅绿 - 初级守护者
  { level: 2, color: [100, 149, 237] }, // 蓝色 - 中级守护者
  { level: 3, color: [255, 165, 0] },   // 橙色 - 高级守护者
  { level: 4, color: [220, 20, 60] },   // 红色 - 资深守护者
  { level: 5, color: [255, 215, 0] },   // 金色 - 荣耀守护者
];

const outDir = path.join(__dirname, '..', 'images');
badges.forEach(({ level, color }) => {
  const png = createPng(color[0], color[1], color[2]);
  const file = path.join(outDir, `badge-${level}.png`);
  fs.writeFileSync(file, png);
  console.log(`Created ${file} (${png.length} bytes)`);
});

console.log('Done! Replace these with real badge icons from your designer.');
