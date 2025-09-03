let autoRefresh = true;
let refreshInterval;
let chaosData = {};

// Color scales for visualizations
const chaosColorScale = d3.scaleSequential(d3.interpolateViridis).domain([0, 1]);
const correlationColorScale = d3.scaleSequential(d3.interpolateRdBu).domain([-1, 1]);

function refreshData() {
  Promise.all([
    fetch('/api/profiling-data').then(r => r.text()).then(parseTransit),
    fetch('/api/chaos-analysis').then(r => r.text()).then(parseTransit),
    fetch('/api/sampling-stats').then(r => r.text()).then(parseTransit)
  ]).then(([profiling, chaos, sampling]) => {
    updateStatusCards(profiling, chaos, sampling);
    updateFlameChart(profiling.flame_tree);
    updateChaosAnalysis(chaos);
    updateCorrelationViz(chaos);
    updateEntropyViz(chaos);
    updateFractalViz(chaos);
    updateCorrelationMatrix(chaos);
    chaosData = chaos;
  }).catch(console.error);
}

function parseTransit(text) {
  // Simple JSON fallback - in production use transit-js
  return JSON.parse(text);
}

function updateStatusCards(profiling, chaos, sampling) {
  document.getElementById('sampling-rate').textContent = 
    (sampling.effective_rate * 100).toFixed(2) + '%';
  document.getElementById('circuit-status').textContent = 
    sampling.overhead_violations > 10 ? 'OPEN' : 'CLOSED';
  document.getElementById('overhead-violations').textContent = 
    sampling.overhead_violations;
  document.getElementById('functions-tracked').textContent = 
    Object.keys(profiling.function_metrics || {}).length;
  
  // Calculate chaos index (simplified)
  const chaosIndex = chaos.execution_analysis ? 
    Math.abs(chaos.execution_analysis.lyapunov_exponent || 0) : 0;
  document.getElementById('chaos-level').textContent = chaosIndex.toFixed(3);
  
  const complexityScore = chaos.execution_analysis ? 
    (chaos.execution_analysis.lz_complexity || 0) / 100 : 0;
  document.getElementById('complexity-score').textContent = complexityScore.toFixed(2);
}

function updateFlameChart(flameTree) {
  const container = d3.select('#flamechart');
  container.selectAll('*').remove();
  
  if (!flameTree || Object.keys(flameTree).length === 0) {
    container.append('div')
      .attr('class', 'loading')
      .html('🔍 Collecting profiling samples...<br><small>Reduce sampling rate if no data appears</small>');
    return;
  }
  
  const svg = container.append('svg')
    .attr('width', '100%')
    .attr('height', '100%')
    .attr('viewBox', '0 0 1200 600');
  
  // Enhanced flamechart with better visual hierarchy
  const flameData = Object.entries(flameTree)
    .map(([name, data]) => ({
      name: name,
      value: data.total_time || data.value || 1,
      calls: data.calls || 1,
      min_time: data.min_time || 0,
      max_time: data.max_time || data.value || 1
    }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 20); // Top 20 functions
  
  if (flameData.length === 0) return;
  
  const maxValue = flameData[0].value;
  const colorScale = d3.scaleOrdinal(d3.schemeSet3);
  const barHeight = 25;
  const spacing = 2;
  
  flameData.forEach((d, i) => {
    const barWidth = (d.value / maxValue) * 800;
    const y = i * (barHeight + spacing) + 20;
    
    // Gradient bar based on performance characteristics
    const defs = svg.append('defs');
    const gradient = defs.append('linearGradient')
      .attr('id', `gradient-${i}`)
      .attr('x1', '0%').attr('x2', '100%');
    
    gradient.append('stop')
      .attr('offset', '0%')
      .attr('stop-color', colorScale(i))
      .attr('stop-opacity', 0.8);
    
    gradient.append('stop')
      .attr('offset', '100%')
      .attr('stop-color', colorScale(i))
      .attr('stop-opacity', 0.4);
    
    // Main bar
    const rect = svg.append('rect')
      .attr('x', 150)
      .attr('y', y)
      .attr('width', barWidth)
      .attr('height', barHeight)
      .attr('fill', `url(#gradient-${i})`)
      .attr('stroke', '#fff')
      .attr('stroke-width', 1)
      .attr('rx', 4);
    
    // Function name
    svg.append('text')
      .attr('x', 145)
      .attr('y', y + barHeight/2)
      .attr('text-anchor', 'end')
      .attr('dominant-baseline', 'central')
      .attr('font-size', '11px')
      .attr('font-weight', '600')
      .attr('fill', '#2d3748')
      .text(d.name.length > 25 ? d.name.substring(0, 22) + '...' : d.name);
    
    // Performance stats
    svg.append('text')
      .attr('x', 155 + barWidth)
      .attr('y', y + barHeight/2)
      .attr('dominant-baseline', 'central')
      .attr('font-size', '10px')
      .attr('fill', '#718096')
      .text(`${formatTime(d.value)} (${d.calls} calls, avg: ${formatTime(d.value/d.calls)})`);
    
    // Interactive tooltip
    rect.on('mouseover', function(event) {
      const tooltip = d3.select('body').append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);
      
      tooltip.transition().duration(200).style('opacity', 1);
      tooltip.html(`
        <strong>${d.name}</strong><br/>
        Total Time: ${formatTime(d.value)}<br/>
        Calls: ${d.calls.toLocaleString()}<br/>
        Average: ${formatTime(d.value / d.calls)}<br/>
        Min: ${formatTime(d.min_time)}<br/>
        Max: ${formatTime(d.max_time)}
      `)
        .style('left', (event.pageX + 15) + 'px')
        .style('top', (event.pageY - 10) + 'px');
    })
    .on('mouseout', () => d3.selectAll('.tooltip').remove());
  });
}

function updateChaosAnalysis(chaos) {
  const container = d3.select('#chaos-analysis');
  container.selectAll('*').remove();
  
  if (!chaos.execution_analysis) {
    container.append('div').attr('class', 'loading').text('Collecting chaos data...');
    return;
  }
  
  const exec = chaos.execution_analysis;
  const metrics = [
    {label: 'Lyapunov Exponent', value: exec.lyapunov_exponent || 0, 
     desc: 'Chaos sensitivity (>0 = chaotic)'},
    {label: 'Shannon Entropy', value: exec.entropy || 0, 
     desc: 'Information content'},
    {label: 'Fractal Dimension', value: exec.fractal_dimension || 0, 
     desc: 'Geometric complexity'},
    {label: 'LZ Complexity', value: exec.lz_complexity || 0, 
     desc: 'Algorithmic complexity'},
    {label: 'Sample Count', value: exec.sample_count || 0, 
     desc: 'Data points analyzed'}
  ];
  
  metrics.forEach(metric => {
    const div = container.append('div').attr('class', 'chaos-metric');
    div.append('div')
      .attr('class', 'chaos-value')
      .style('color', chaosColorScale(Math.abs(metric.value) / 10))
      .text(typeof metric.value === 'number' ? metric.value.toFixed(4) : metric.value);
    div.append('div')
      .attr('class', 'chaos-label')
      .text(metric.label);
    div.append('div')
      .style('font-size', '0.8em')
      .style('color', '#718096')
      .style('margin-top', '5px')
      .text(metric.desc);
  });
}

function updateCorrelationViz(chaos) {
  const container = d3.select('#correlation-viz');
  container.selectAll('*').remove();
  
  if (!chaos.execution_analysis?.autocorrelations) {
    container.append('div').attr('class', 'loading').text('Computing correlations...');
    return;
  }
  
  const svg = container.append('svg')
    .attr('width', '100%')
    .attr('height', '100%')
    .attr('viewBox', '0 0 400 400');
  
  const autocorr = chaos.execution_analysis.autocorrelations;
  const lags = Object.keys(autocorr).map(Number).sort((a,b) => a-b);
  const values = lags.map(lag => autocorr[lag]);
  
  // Simple line chart for autocorrelations
  const xScale = d3.scaleLinear().domain(d3.extent(lags)).range([40, 360]);
  const yScale = d3.scaleLinear().domain(d3.extent(values)).range([360, 40]);
  
  const line = d3.line()
    .x(d => xScale(d.lag))
    .y(d => yScale(d.value));
  
  const data = lags.map((lag, i) => ({lag, value: values[i]}));
  
  svg.append('path')
    .datum(data)
    .attr('fill', 'none')
    .attr('stroke', '#667eea')
    .attr('stroke-width', 3)
    .attr('d', line);
  
  // Add circles for data points
  svg.selectAll('.dot')
    .data(data)
    .enter().append('circle')
    .attr('class', 'dot')
    .attr('cx', d => xScale(d.lag))
    .attr('cy', d => yScale(d.value))
    .attr('r', 4)
    .attr('fill', '#764ba2');
  
  // Axes
  svg.append('g')
    .attr('transform', 'translate(0,360)')
    .call(d3.axisBottom(xScale));
  
  svg.append('g')
    .attr('transform', 'translate(40,0)')
    .call(d3.axisLeft(yScale));
}

function updateEntropyViz(chaos) {
  // Simplified entropy visualization
  const container = d3.select('#entropy-viz');
  container.selectAll('*').remove();
  
  if (!chaos.execution_analysis?.entropy) {
    container.append('div').attr('class', 'loading').text('Computing entropy...');
    return;
  }
  
  const entropy = chaos.execution_analysis.entropy;
  const maxEntropy = Math.log2(32); // Assuming 32 bins
  
  const svg = container.append('svg')
    .attr('width', '100%')
    .attr('height', '100%')
    .attr('viewBox', '0 0 400 400');
  
  // Entropy meter
  const angle = (entropy / maxEntropy) * Math.PI;
  const radius = 120;
  
  svg.append('circle')
    .attr('cx', 200)
    .attr('cy', 200)
    .attr('r', radius)
    .attr('fill', 'none')
    .attr('stroke', '#e2e8f0')
    .attr('stroke-width', 20);
  
  svg.append('path')
    .attr('d', d3.arc()
      .innerRadius(radius - 10)
      .outerRadius(radius + 10)
      .startAngle(-Math.PI/2)
      .endAngle(-Math.PI/2 + angle))
    .attr('transform', 'translate(200,200)')
    .attr('fill', chaosColorScale(entropy / maxEntropy));
  
  svg.append('text')
    .attr('x', 200)
    .attr('y', 200)
    .attr('text-anchor', 'middle')
    .attr('dominant-baseline', 'central')
    .attr('font-size', '24px')
    .attr('font-weight', 'bold')
    .text(entropy.toFixed(3));
}

function updateFractalViz(chaos) {
  // Placeholder for fractal dimension visualization
  const container = d3.select('#fractal-viz');
  container.selectAll('*').remove();
  
  const fractalDim = chaos.execution_analysis?.fractal_dimension || 0;
  
  container.append('div')
    .style('text-align', 'center')
    .style('padding', '50px')
    .html(`<div style="font-size: 3em; font-weight: bold; color: #667eea;">${fractalDim.toFixed(4)}</div>
          <div style="margin-top: 10px; color: #666;">Fractal Dimension</div>
          <div style="margin-top: 10px; font-size: 0.9em; color: #999;">
          ${fractalDim > 1.5 ? 'Complex, fractal-like behavior' : 'Simple, regular patterns'}</div>`);
}

function updateCorrelationMatrix(chaos) {
  const container = d3.select('#correlation-matrix');
  container.selectAll('*').remove();
  
  if (!chaos.cross_correlations) {
    container.append('div').attr('class', 'loading').text('Computing cross-correlations...');
    return;
  }
  
  const corr = chaos.cross_correlations;
  const matrix = [
    ['Exec-Mem', corr.execution_memory || 0],
    ['Exec-Thread', corr.execution_threads || 0]
  ];
  
  matrix.forEach(([label, value]) => {
    const cell = container.append('div')
      .attr('class', 'correlation-cell')
      .style('background-color', correlationColorScale(value))
      .text(`${label}: ${value.toFixed(3)}`);
  });
}

function formatTime(nanoseconds) {
  if (nanoseconds < 1000) return nanoseconds.toFixed(0) + 'ns';
  if (nanoseconds < 1000000) return (nanoseconds / 1000).toFixed(1) + 'μs';  
  if (nanoseconds < 1000000000) return (nanoseconds / 1000000).toFixed(1) + 'ms';
  return (nanoseconds / 1000000000).toFixed(2) + 's';
}

function toggleSampling() {
  fetch('/api/toggle-sampling', {method: 'POST'})
    .then(() => refreshData());
}

function exportData() {
  const data = {
    chaos: chaosData,
    timestamp: new Date().toISOString()
  };
  const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `flamechart-chaos-${Date.now()}.json`;
  a.click();
}

function resetCircuitBreaker() {
  fetch('/api/reset-circuit', {method: 'POST'})
    .then(() => refreshData());
}

// Auto-refresh
setInterval(refreshData, 5000);
refreshData();