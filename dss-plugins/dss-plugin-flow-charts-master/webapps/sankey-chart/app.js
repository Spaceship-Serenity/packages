let allRows;

function draw() {
    const cfg = dataiku.getWebAppConfig()['webAppConfig'];
    let data = new google.visualization.DataTable();
    let min_weight = 0;
    data.addColumn('string', 'From');
    data.addColumn('string', 'To');
    data.addColumn('number', 'Weight');
    let rows = allRows
        .filter(r => r[2] > min_weight)
        .map(r => [r[0], r[1], r[2]]);
    data.addRows(rows);
    let chart = new google.visualization.Sankey(document.getElementById('sankey'));
    chart.draw(data, {});
}
  
let cfg = dataiku.getWebAppConfig()['webAppConfig'];
initSankey(cfg, (data) => {
    allRows = data;
    draw(); 
});

window.addEventListener('message', function(event) {
   if (event.data) {
       let cfg = JSON.parse(event.data)['webAppConfig'];
       dataiku.resolvePluginConfig(cfg, function(data) {
           initSankey(cfg, (data) => {
                allRows = data;
                draw(); 
            });
       }, function(data) {
           console.warn("faile to resolve", data);
       })
   }
});