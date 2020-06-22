let allRows;
let webAppConfig = dataiku.getWebAppConfig()['webAppConfig'];

function getMostCommonSource() {
    let counts = {}
    allRows.forEach(r => {
        counts[r[0]] = (counts[r[0]]||0) + 1;
    });
    let reducer = (x, curr) => counts[x] > counts[curr] ? x : curr;
    return Object.keys(counts).reduce(reducer, -Infinity)
}


function loadState(stateName) {
    const max_links = webAppConfig['max_links'] || 12;
    const min_weight = webAppConfig['min_weight'] || 0;
    
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'From');
    data.addColumn('string', 'To');
    data.addColumn('number', 'Weight');
    let pre = allRows
        .filter(r => r[1] == stateName && r[0] != stateName)
        .sort((a, b) => b[2] - a[2])
        .slice(0, max_links)
    let post = allRows
        .filter(r => r[0] == stateName && r[1] != stateName)
        .map(r => [r[0], r[1]+' ', r[2]])
        .sort((a, b) => b[2] - a[2])
        .slice(0, max_links)
    rows = pre.concat(post);
    if (!rows.length) {
        return webappMessages.displayFatalError('Nothing to display.');
    }
    data.addRows(rows);
    let options = {
        sankey: {
            node: {
                interactivity: true
            }
        }
    };
    let chart = new google.visualization.Sankey(document.getElementById('sankey'));
    chart.draw(data, options);
    google.visualization.events.addListener(chart, 'select', function() {
        if (chart.getSelection()[0].name) {
            loadState(chart.getSelection()[0].name.trim())
        }
    });
}

let cfg = dataiku.getWebAppConfig()['webAppConfig'];
initSankey(cfg, (data) => {
    allRows = data;
    loadState(getMostCommonSource()); 
});


// window.addEventListener('message', function(event) {
//     if (event.data) {
//         let chartDef = JSON.parse(event.data);
//         let webAppConfig = chartDef['webAppConfig']
//         console.warn('got cfg from form:', webAppConfig);
//         dataiku.resolvePluginConfig(webAppConfig, function(data) {
//             const event = new Event('webAppConfigChanged');
//             event.data = data;
//             window.dispatchEvent(event);
//         }, function(data) {console.warn("faile to resolve", data)})
       
//     }
// });

window.addEventListener('message', function(event) {
   if (event.data) {
       let cfg = JSON.parse(event.data)['webAppConfig'];
       dataiku.resolvePluginConfig(cfg, function(data) {
            initSankey(cfg, (data) => {
                allRows = data;
                loadState(getMostCommonSource()); 
            });
       }, function(data) {
           console.warn("faile to resolve", data);
       })
   }
});