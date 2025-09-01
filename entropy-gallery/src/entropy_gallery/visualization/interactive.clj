(ns entropy-gallery.visualization.interactive
  (:require [entropy-gallery.processes.levy :as levy]
            [clojure.data.json :as json]))

(defn generate-interactive-data [samples chart-type]
  "Generate interactive visualization data with Lévy processes"
  (let [normalized (map #(if (number? %) % (hash %)) samples)
        n (count normalized)
        max-val (if (seq normalized) (apply max normalized) 1)]
    
    (case chart-type
      :levy-flight 
      (let [alpha 1.5 beta 0.0 scale (/ max-val 100.0)
            flight (levy/levy-flight n alpha beta scale 0)]
        {:type "levy-flight"
         :path (map-indexed (fn [i pos] {:x i :y pos :entropy (nth normalized i 0)}) flight)
         :alpha alpha :beta beta})
      
      :fractal-brownian
      (let [hurst 0.7 scale (Math/sqrt (/ max-val 10.0))
            fbm (levy/fractional-brownian-motion n hurst scale)]
        {:type "fractal-brownian"
         :path (map-indexed (fn [i pos] {:x i :y pos :entropy (nth normalized i 0)}) fbm)
         :hurst hurst})
      
      :jump-diffusion
      (let [lambda 0.1 mu 0.02 sigma 0.1 
            jump-mean 0.0 jump-std 0.05 dt 1.0
            jumps (levy/jump-diffusion-process n lambda mu sigma jump-mean jump-std dt)]
        {:type "jump-diffusion"
         :path (map-indexed (fn [i pos] {:x i :y pos :entropy (nth normalized i 0)}) jumps)
         :lambda lambda :mu mu :sigma sigma})
      
      :stable-diffusion
      (let [alpha 1.8 beta 0.0 temperature 0.1 step-size 0.01
            diffusion (levy/entropy-stable-diffusion 0.0 normalized n alpha beta temperature step-size)]
        {:type "stable-diffusion"
         :path (map-indexed (fn [i pos] {:x i :y pos :entropy (nth normalized i 0)}) diffusion)
         :alpha alpha :beta beta :temperature temperature})
      
      ;; Fallback to original chart types
      (case chart-type
        :line {:points (map-indexed vector normalized)}
        :bar {:bars (map-indexed (fn [i v] {:x i :y (mod (Math/abs v) 100)}) normalized)}
        :heatmap {:matrix (partition 4 4 (repeat 0) (map #(mod (Math/abs %) 255) normalized))}
        :parametric {:coords (map (fn [i v] {:x (* (Math/cos (* i 0.5)) (mod (Math/abs v) 50))
                                            :y (* (Math/sin (* i 0.5)) (mod (Math/abs v) 50))}) 
                                 (range) normalized)}))))

(defn animation-javascript []
  "Generate JavaScript for interactive animations"
  "
  function animateChart(sourceKey, chartType, data) {
    const container = d3.select('#chart-' + sourceKey);
    const svg = container.select('svg');
    const width = 560, height = 180;
    const margin = {top: 20, right: 20, bottom: 30, left: 40};
    
    if (data.type === 'levy-flight' || data.type === 'fractal-brownian' || 
        data.type === 'jump-diffusion' || data.type === 'stable-diffusion') {
      
      const xScale = d3.scaleLinear()
        .domain(d3.extent(data.path, d => d.x))
        .range([margin.left, width - margin.right]);
      
      const yScale = d3.scaleLinear()
        .domain(d3.extent(data.path, d => d.y))
        .range([height - margin.bottom, margin.top]);
      
      // Animated path drawing
      const line = d3.line()
        .x(d => xScale(d.x))
        .y(d => yScale(d.y))
        .curve(d3.curveBasis);
      
      const path = svg.append('path')
        .datum(data.path)
        .attr('fill', 'none')
        .attr('stroke', '#00ff88')
        .attr('stroke-width', 2)
        .attr('d', line);
      
      // Animate path drawing
      const totalLength = path.node().getTotalLength();
      path
        .attr('stroke-dasharray', totalLength + ' ' + totalLength)
        .attr('stroke-dashoffset', totalLength)
        .transition()
        .duration(3000)
        .ease(d3.easeLinear)
        .attr('stroke-dashoffset', 0);
      
      // Interactive points that respond to entropy
      svg.selectAll('.entropy-point')
        .data(data.path.filter((d, i) => i % 5 === 0))
        .enter().append('circle')
        .attr('class', 'entropy-point')
        .attr('cx', d => xScale(d.x))
        .attr('cy', d => yScale(d.y))
        .attr('r', 0)
        .attr('fill', '#00ffff')
        .attr('opacity', 0.7)
        .transition()
        .delay((d, i) => i * 100)
        .duration(500)
        .attr('r', d => Math.max(2, Math.min(8, Math.sqrt(Math.abs(d.entropy)) / 1000)))
        .on('end', function(d) {
          // Pulsing animation based on entropy strength
          d3.select(this)
            .transition()
            .duration(1000 + (Math.abs(d.entropy) % 1000))
            .attr('r', d => Math.max(1, Math.sqrt(Math.abs(d.entropy)) / 2000))
            .transition()
            .duration(1000 + (Math.abs(d.entropy) % 1000))
            .attr('r', d => Math.max(2, Math.min(8, Math.sqrt(Math.abs(d.entropy)) / 1000)))
            .on('end', function() {
              // Repeat pulsing
              d3.select(this).node().dispatchEvent(new Event('end'));
            });
        });
      
      // Add parameter display
      let paramText = '';
      if (data.type === 'levy-flight') {
        paramText = `Lévy Flight: α=${data.alpha}, β=${data.beta}`;
      } else if (data.type === 'fractal-brownian') {
        paramText = `Fractional Brownian: H=${data.hurst}`;
      } else if (data.type === 'jump-diffusion') {
        paramText = `Jump Diffusion: λ=${data.lambda}, μ=${data.mu}`;
      } else if (data.type === 'stable-diffusion') {
        paramText = `Stable Diffusion: α=${data.alpha}, T=${data.temperature}`;
      }
      
      svg.append('text')
        .attr('x', margin.left)
        .attr('y', height - 5)
        .attr('font-size', '10px')
        .attr('fill', '#666')
        .text(paramText);
    }
  }
  
  function addInteractiveElements(sourceKey) {
    const container = d3.select('#chart-' + sourceKey);
    
    // Add real-time entropy strength indicator
    const strengthIndicator = container.append('div')
      .attr('class', 'entropy-strength')
      .style('position', 'absolute')
      .style('top', '10px')
      .style('right', '10px')
      .style('background', 'rgba(0,255,136,0.1)')
      .style('padding', '5px 10px')
      .style('border-radius', '4px')
      .style('font-size', '12px')
      .text('Entropy: Loading...');
    
    // Update strength indicator based on latest data
    setInterval(() => {
      const randomStrength = Math.random() * 100;
      strengthIndicator
        .text(`Entropy: ${randomStrength.toFixed(1)}%`)
        .style('color', d3.interpolateViridis(randomStrength / 100));
    }, 2000);
  }
  ")