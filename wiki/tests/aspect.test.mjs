import test from 'node:test';
import assert from 'node:assert/strict';
import { ASPECT_PRESETS, aspectGeometry } from '../public/assets/view/zoom.js';
test('preset order matches the installed Prism 1.1.0 configuration',()=>{
 assert.deepEqual(ASPECT_PRESETS.map(p=>p.label),['Auto','2.39:1','2.35:1','2:1','1.85:1','16:9','16:10','4:3','1:1','Legacy']);
});
test('wide presets letterbox and narrow presets pillarbox',()=>{
 const wide=aspectGeometry(16/9,{ratio:2.39});
 assert.equal(wide.side,0);assert.ok(Math.abs(wide.top-12.80799628079963)<1e-8);
 const square=aspectGeometry(16/9,{ratio:1});
 assert.equal(square.top,0);assert.equal(square.side,21.875);
 assert.deepEqual(aspectGeometry(16/9,{ratio:0}),{top:0,side:0});
 assert.deepEqual(aspectGeometry(16/9,{ratio:null},15),{top:15,side:0});
});
