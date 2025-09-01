(ns entropy-gallery.visualization.charts)

(defn generate-chart-data [samples chart-type]
  "Generate visualization data for different chart types"
  (let [normalized (map #(if (number? %) % (if (string? %) (hash %) %)) samples)]
    (case chart-type
      :line {:points (map-indexed vector normalized)}
      :bar {:bars (map-indexed (fn [i v] {:x i :y (mod (Math/abs v) 100)}) normalized)}
      :heatmap {:matrix (partition 4 4 (repeat 0) (map #(mod (Math/abs %) 255) normalized))}
      :parametric {:coords (map (fn [i v] {:x (* (Math/cos (* i 0.5)) (mod (Math/abs v) 50))
                                          :y (* (Math/sin (* i 0.5)) (mod (Math/abs v) 50))}) 
                               (range) normalized)}
      :histogram {:buckets (->> normalized
                               (map #(int (/ (mod (Math/abs %) 1000) 100)))
                               frequencies
                               (sort-by first))})))

(defn chart-javascript []
  "Generate D3.js visualization code"
  "
    function showChart(sourceKey, chartType) {
      const container = d3.select('#chart-' + sourceKey);
      container.selectAll('*').remove();
      
      const data = sourceData[sourceKey];
      const chartData = data.chart_data[chartType];
      
      // Update active tab
      d3.selectAll('#card-' + sourceKey + ' .chart-tab').classed('active', false);
      d3.select('#card-' + sourceKey + ' .chart-tab').filter(function() { 
        return this.textContent === chartType.charAt(0).toUpperCase() + chartType.slice(1); 
      }).classed('active', true);
      
      const svg = container.append('svg')
        .attr('width', '100%')
        .attr('height', '100%');
        
      const width = 560, height = 180;
      const margin = {top: 20, right: 20, bottom: 30, left: 40};
      
      if (chartType === 'line') {
        const xScale = d3.scaleLinear().domain([0, chartData.points.length-1]).range([margin.left, width-margin.right]);
        const yScale = d3.scaleLinear().domain(d3.extent(chartData.points, d => d[1])).range([height-margin.bottom, margin.top]);
        
        const line = d3.line()
          .x(d => xScale(d[0]))
          .y(d => yScale(d[1]));
          
        svg.append('path')
          .datum(chartData.points)
          .attr('class', 'parametric-path')
          .attr('d', line);
      }
      
      if (chartType === 'bar') {
        const xScale = d3.scaleBand().domain(chartData.bars.map(d => d.x)).range([margin.left, width-margin.right]).padding(0.1);
        const yScale = d3.scaleLinear().domain([0, d3.max(chartData.bars, d => d.y)]).range([height-margin.bottom, margin.top]);
        
        svg.selectAll('.bar')
          .data(chartData.bars)
          .enter().append('rect')
          .attr('class', 'bar')
          .attr('x', d => xScale(d.x))
          .attr('y', d => yScale(d.y))
          .attr('width', xScale.bandwidth())
          .attr('height', d => height - margin.bottom - yScale(d.y))
          .attr('fill', '#00ff88')
          .attr('opacity', 0.7);
      }
      
      if (chartType === 'heatmap') {
        const cellSize = Math.min(width/4, height/4) - 2;
        chartData.matrix.forEach((row, i) => {
          row.forEach((cell, j) => {
            svg.append('rect')
              .attr('class', 'heatmap-cell')
              .attr('x', j * (cellSize + 2) + 20)
              .attr('y', i * (cellSize + 2) + 20)
              .attr('width', cellSize)
              .attr('height', cellSize)
              .attr('fill', d3.interpolateViridis(cell / 255));
          });
        });
      }
      
      if (chartType === 'parametric') {
        const xScale = d3.scaleLinear().domain(d3.extent(chartData.coords, d => d.x)).range([margin.left, width-margin.right]);
        const yScale = d3.scaleLinear().domain(d3.extent(chartData.coords, d => d.y)).range([height-margin.bottom, margin.top]);
        
        const path = d3.line()
          .x(d => xScale(d.x))
          .y(d => yScale(d.y))
          .curve(d3.curveBasis);
          
        svg.append('path')
          .datum(chartData.coords)
          .attr('class', 'parametric-path')
          .attr('d', path);
      }
    }
    
    function toggleAccordion(sourceKey) {
      const panel = document.getElementById('panel-' + sourceKey);
      panel.style.display = panel.style.display === 'block' ? 'none' : 'block';
    }
  ")