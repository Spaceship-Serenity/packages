let dataReady;
let chartReady;

function initSankey(cfg, onReady) {
    try {
        dataiku.checkWebAppParameters();
    } catch (e) {
        console.error(e);
        webappMessages.displayFatalError(e.message + ' Go to settings tab.');
        return;
    }
    
    const dataset = cfg['dataset'];
    const sampling = {};
    const from_col = cfg['from_col'];
    const to_col = cfg['to_col'];
    const weight_col = cfg['weight_col'];
    console.warn('WEIGHT COL: ', weight_col); 
    const max_links = cfg['max_links'] || 12;
    const min_weight = cfg['min_weight'] || 0;
    let allRows;
    
    function drawAppIfEverythingReady() {
        if (!chartReady || !dataReady) {
            return;
        }
        onReady(allRows);
    }

    if (!window.google) {
        webappMessages.displayFatalError('Failed to load Google Charts library. Check your connection.');
    } else {
        google.charts.load('current', {'packages':['sankey']});
        google.charts.setOnLoadCallback(function() {
            chartReady = true;
            drawAppIfEverythingReady();
        });
        dataiku.fetch(dataset, sampling, function(dataFrame) {
            allRows = dataFrame.mapRecords(r => [r[from_col], r[to_col], +r[weight_col]]).filter(r => r[2] > min_weight);
            dataReady = true;
            drawAppIfEverythingReady()
        });
    }
}