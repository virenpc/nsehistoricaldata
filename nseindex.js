var casper = require('casper').create({
  // verbose: true,
  // logLevel: "debug",
  pageSettings: {
    userAgent: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11"
  }
});
var xpath = require('casper').selectXPath;

var symbolList = [
    'CNX AUTO','CNX BANK','CNX ENERGY','CNX FINANCE','CNX FMCG','CNX IT','CNX MEDIA','CNX METAL','CNX PHARMA','CNX PSU BANK','CNX REALTY'
];

var fromDate = '10-03-2014';
var toDate = '14-03-2014';
var urlPre='http://www.nseindia.com/products/dynaContent/equities/indices/historicalindices.jsp?indexType=';

function getLink() {
    var links = document.querySelectorAll('a');
    return Array.prototype.map.call(links, function(e) {
        return e.getAttribute('href');
    });
}

casper.start();
 
 var j=0;
casper.then(function() {
    
    for (var i = 0 ; i < symbolList.length ; i++) {
      casper.thenOpen(urlPre+symbolList[i]+'&fromDate='+fromDate+'&toDate='+toDate, function() {  
			
          casper.download('http://www.nseindia.com/'+this.evaluate(getLink),symbolList[j]+'.csv'); 
          j++;
		});     
	}
});

casper.then(function() {
	casper.echo('Done');
});
casper.run();