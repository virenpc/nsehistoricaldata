var casper = require('casper').create({
  // verbose: true,
  // logLevel: "debug",
  pageSettings: {
    userAgent: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11"
  }
});
var xpath = require('casper').selectXPath;

var symbolList = [
    '20MICRONS','3IINFOTECH','3MINDIA','A2ZMES','AARTIDRUGS','AARTIIND','AARVEEDEN','ABAN','ABB','ABBOTINDIA','ABCIL','ABGSHIP','ABIRLANUVO','ACC','ACCELYA','ACE','ACROPETAL','ADANIENT','ADANIPORTS','ADANIPOWER','ADFFOODS','ADHUNIK','ADORWELD','ADSL','ADVANIHOTR','ADVANTA','AEGISCHEM','AFL','AFTEK','AGCNET','AGRITECH','AGRODUTCH','AHLEAST','AHLUCONT','AHLWEST','AHMEDFORGE','AIAENG','AICHAMP','AIL','AJANTPHARM','AJMERA','AKSHOPTFBR','AKZOINDIA','ALBK','ALCHEM','ALEMBICLTD','ALICON','ALKALI','ALKYLAMINE','ALLCARGO','ALLSEC','ALMONDZ','ALOKTEXT','ALPA','ALPHAGEO','ALPINEHOU','ALPSINDUS','ALSTOMT&D','AMARAJABAT','AMARJOTHI','AMBIKCO','AMBUJACEM','AMDIND','AMLSTEEL','AMRUTANJAN','AMTEKAUTO','AMTEKINDIA','AMTL','ANANTRAJ','ANDHRABANK','ANDHRACEMT','ANDHRSUGAR','ANGIND','ANIKINDS','ANKITMETAL','ANSALAPI','ANSALHSG','ANTGRAPHIC','APARINDS','APCOTEXIND','APLAB','APLAPOLLO','APLLTD','APOLLOHOSP','APOLLOTYRE','APTECHT','ARCHIDPLY','ARCHIES','ARIES','ARIHANT','AROGRANITE','ARROWTEX','ARSHIYA','ARSSINFRA','ARVIND','ARVINDREM','ASAHIINDIA','ASAHISONG','ASAL','ASHAPURMIN','ASHIANA','ASHIMASYN','ASHOKA','ASHOKLEY','ASIANHOTNR','ASIANPAINT','ASIANTILES','ASIL','ASSAMCO','ASTEC','ASTERSILI','ASTRAL','ASTRAMICRO','ASTRAZEN','ATFL','ATLANTA','ATLASCYCLE','ATNINTER','ATUL','ATULAUTO','AURIONPRO','AUROPHARMA','AUSOMENT','AUSTRAL','AUTOAXLES','AUTOIND','AUTOLITIND','AVANTI','AVTNPL','AXIS-IT&T','AXISBANK','BAFNAPHARM','BAGFILMS','BAJAJ-AUTO','BAJAJCORP','BAJAJELEC','BAJAJFINSV','BAJAJHIND','BAJAJHLDNG','BAJFINANCE','BALAJITELE','BALAMINES','BALKRISIND','BALLARPUR','BALMLAWRIE','BALPHARMA','BALRAMCHIN','BANARBEADS','BANARISUG','BANCOINDIA','BANG','BANKBARODA','BANKINDIA','BANSWRAS','BARTRONICS','BASF','BASML','BATAINDIA','BAYERCROP','BBL','BBTC','BEARDSELL','BECREL','BEDMUTHA','BEL','BEML','BEPL','BERGEPAINT','BFINVEST','BFUTILITIE','BGLOBAL','BGRENERGY','BHAGYNAGAR','BHARATFORG','BHARATGEAR','BHARATRAS','BHARTIARTL','BHARTISHIP','BHEL','BHUSANSTL','BIL','BILENERGY','BILPOWER','BIMETAL','BINANIIND','BINDALAGRO','BIOCON','BIRLACORPN','BIRLACOT','BIRLAERIC','BIRLAMONEY','BLBLIMITED','BLISSGVS','BLKASHYAP','BLUECHIP','BLUECOAST','BLUEDART','BLUESTARCO','BLUESTINFO','BODALCHEM','BODHTREE','BOMDYEING','BOSCHLTD','BPCL','BPL','BRANDHOUSE','BRFL','BRIGADE','BRITANNIA','BROADCAST','BROOKS','BSELINFRA','BSL','BSLIMITED','BURNPUR','BVCL','CADILAHC','CAIRN','CALSOFT','CANBK','CANDC','CANFINHOME','CANTABIL','CAPF','CARBORUNIV','CARBORUNIV','CAREERP','CARERATING','CASTROL','CCCL','CCL','CEATLTD','CEBBCO','CELEBRITY','CELESTIAL','CENTENKA','CENTEXT','CENTRALBK','CENTUM','CENTURYPLY','CENTURYTEX','CERA','CEREBRAINT','CESC','CGCL','CHAMBLFERT','CHEMFALKAL','CHENNPETRO','CHESLINTEX','CHOLAFIN','CHROMATIC','CIMMCO','CINELINE','CINEMAXIN','CINEVISTA','CIPLA','CLASSIC','CLNINDIA','CLUTCHAUTO','CMAHENDRA','CMC','CMC','CNOVAPETRO','COALINDIA','COLPAL','COMPUAGE','COMPUSOFT','CONCOR','CONCOR','CONSOFINVT','CORDSCABLE','COREEDUTEC','COROENGG','COROMANDEL','CORPBANK','COSMOFILMS','COUNCODOS','COX&KINGS','CREATIVEYE','CRESTANI','CRISIL','CROMPGREAV','CTE','CUB','CUB','CUBEXTUB','CUMMINSIND','CURATECH','CYBERMEDIA','CYBERTECH','DAAWAT','DABUR','DALMIABHA','DALMIASUG','DATAMATICS','DBCORP','DBREALTY','DBSTOCKBRO','DCBBANK','DCM','DCMSRMCONS','DCW','DECCANCE','DECOLIGHT','DEEPAKFERT','DEEPAKNTR','DEEPIND','DELTACORP','DELTAMAGNT','DEN','DENABANK','DENORA','DHAMPURSUG','DHANBANK','DHANUKA','DHARSUGAR','DHFL','DHUNINV','DIAPOWER','DICIND','DIGJAM','DISHMAN','DISHTV','DIVISLAB','DLF','DLINKINDIA','DOLPHINOFF','DONEAR','DPSCLTD','DPTL','DQE','DRDATSONS','DREDGECORP','DRREDDY','DSKULKARNI','DSSL','DUNCANSLTD','DWARKESH','DYNAMATECH','DYNAMATECH','DYNATECH','EASTSILK','EASUNREYRL','ECEIND','ECLERX','EDELWEISS','EDELWEISS','EDL','EDUCOMP','EICHERMOT','EIDPARRY','EIHAHOTELS','EIHOTEL','EIMCOELECO','EKC','ELDERPHARM','ELECON','ELECTCAST','ELECTHERM','ELFORGE','ELGIEQUIP','ELGIRUBCO','ELNET','EMAMIINFRA','EMAMILTD','EMCO','EMKAY','EMMBI','EMPEESUG','ENERGYDEV','ENGINERSIN','ENIL','ENTEGRA','EON','ERAINFRA','EROSMEDIA','ESABINDIA','ESCORTS','ESL','ESSAROIL','ESSARPORTS','ESSARSHPNG','ESSDEE','ESSELPACK','ESTER','EUROCERA','EUROMULTI','EUROTEXIND','EVEREADY','EVERESTIND','EVERONN','EXCELCROP','EXCELINDUS','EXCELINFO','EXIDEIND','FACT','FAGBEARING','FARMAXIND','FCEL','FCSSOFT','FDC','FEDDERLOYD','FEDERALBNK','FEDERALBNK','FIEMIND','FILATEX','FINANTECH','FINCABLES','FINPIPE','FIRSTWIN','FLEXITUFF','FLFL','FMGOETZE','FMNL','FORTIS','FOSECOIND','FRL','FRL','FRLDVR','FRLDVR','FSL','GABRIEL','GAEL','GAIL','GAL','GALLANTT','GALLISPAT','GAMMNINFRA','GAMMONIND','GANDHITUBE','GANESHHOUC','GANGOTRI','GARDENSILK','GARWALLROP','GATI','GAYAPROJ','GDL','GEECEE','GEINDSYS','GEMINI','GENESYS','GENUSPOWER','GEOJITBNPP','GEOMETRIC','GESHIP','GESHIP','GHCL','GICHSGFIN','GILLANDERS','GILLETTE','GINNIFILA','GIPCL','GISOLUTION','GITANJALI','GKB','GKWLIMITED','GLAXO','GLENMARK','GLFL','GLOBALVECT','GLOBOFFS','GLOBUSSPR','GLODYNE','GLORY','GMBREW','GMDCLTD','GMRINFRA','GNFC','GOACARBON','GODFRYPHLP','GODREJCP','GODREJIND','GODREJPROP','GOENKA','GOKEX','GOKUL','GOLDENTOBC','GOLDIAM','GOLDINFRA','GOLDTECH','GPIL','GPPL','GRANULES','GRAPHITE','GRASIM','GRASIM','GRAVITA','GREAVESCOT','GREENFIRE','GREENPLY','GREENPOWER','GRINDWELL','GRUH','GSCLCEMENT','GSFC','GSKCONS','GSLNOVA','GSPL','GSS','GSS','GTL','GTLINFRA','GTNIND','GTNTEX','GTOFFSHORE','GUFICBIO','GUJALKALI','GUJAPOLLO','GUJFLUORO','GUJNRECOKE','GUJNREDVR','GUJRATGAS','GUJSTATFIN','GULFOILCOR','GVKPIL','HANUNG','HARITASEAT','HARRMALAYA','HATHWAY','HAVELLS','HBLPOWER','HBSTOCK','HCC','HCIL','HCL-INSYS','HCLTECH','HDFC','HDFCBANK','HDFCBANK','HDIL','HEG','HEIDELBERG','HELIOSMATH','HERCULES','HERITGFOOD','HEROMOTOCO','HEXATRADEX','HEXAWARE','HFCL','HGS','HIKAL','HIL','HILTON','HIMATSEIDE','HINDALCO','HINDCOMPOS','HINDCOPPER','HINDDORROL','HINDMOTORS','HINDNATGLS','HINDOILEXP','HINDPETRO','HINDSYNTEX','HINDUJAFO','HINDUJAVEN','HINDUNILVR','HINDZINC','HIRAFERRO','HIRECT','HITACHIHOM','HITECHGEAR','HITECHPLAS','HMT','HMVL','HOCL','HONAUT','HONDAPOWER','HORIZONINF','HOTELEELA','HOTELRUGBY','HOVS','HSIL','HTMEDIA','HUBTOWN','HYDROS&S','IBIPL','IBPOW','IBREALEST','IBSEC','IBULHSGFIN','IBWSL','ICICIBANK','ICIL','ICRA','ICSA','IDBI','IDEA','IDFC','IFBAGRO','IFBIND','IFCI','IFGLREFRAC','IGARASHI','IGL','IGPL','IITL','IL&FSENGG','IL&FSTRANS','IMFA','IMPAL','IMPEXFERRO','INDBANK','INDHOTEL','INDIACEM','INDIAGLYCO','INDIAINFO','INDIANB','INDIANCARD','INDIANHUME','INDLMETER','INDNIPPON','INDOCO','INDORAMA','INDOSOLAR','INDOTECH','INDOTHAI','INDOWIND','INDRAMEDCO','INDSWFTLAB','INDSWFTLTD','INDTERRAIN','INDUSFILA','INDUSINDBK','INFINITE','INFODRIVE','INFOMEDIA','INFOTECENT','INFRATEL','INFY','INGERRAND','INGVYSYABK','INGVYSYABK','INNOIND','INOXLEISUR','INSECTICID','INTEGRA','INVENTURE','IOB','IOC','IOLCP','IPAPPM','IPCALAB','IPCALAB','IPRINGLTD','IRB','ISFT','ISMTLTD','ITC','ITDCEM','ITI','IVC','IVP','IVRCLINFRA','J&KBANK','JAGRAN','JAGSNPHARM','JAIBALAJI','JAICORPLTD','JAIHINDPRO','JAINSTUDIO','JAMNAAUTO','JAYAGROGN','JAYBARMARU','JAYNECOIND','JAYSREETEA','JBCHEPHARM','JBFIND','JBMA','JCTEL','JDORGOCHEM','JENSONICOL','JETAIRWAYS','JEYPORE','JHS','JIKIND','JINDALPHOT','JINDALPOLY','JINDALSAW','JINDALSTEL','JINDCOT','JINDRILL','JINDWORLD','JISLDVREQS','JISLJALEQS','JKCEMENT','JKIL','JKLAKSHMI','JKPAPER','JKTYRE','JMCPROJECT','JMFINANCIL','JMTAUTOLTD','JOCIL','JPASSOCIAT','JPINFRATEC','JPOLYINVST','JPPOWER','JSL','JSWENERGY','JSWHL','JSWSTEEL','JUBILANT','JUBLFOOD','JUBLFOOD','JUBLINDS','JUMBO','JUSTDIAL','JVLAGRO','JYOTHYLAB','JYOTISTRUC','KABRAEXTRU','KAJARIACER','KAKATCEM','KALINDEE','KALPATPOWR','KALYANIFRG','KAMATHOTEL','KANANIIND','KANDAGIRI','KANORICHEM','KANSAINER','KARMAENG','KARURKCP','KARURVYSYA','KAUSHALYA','KAVVERITEL','KBIL','KCP','KCPSUGIND','KEC','KECL','KEI','KEMROCK','KERNEX','KESARENT','KESORAMIND','KEYCORPSER','KFA','KGL','KHAITANELE','KHAITANLTD','KHANDSE','KICL','KIL','KILITCH','KIRIINDUS','KIRLOSBROS','KIRLOSENG','KIRLOSIND','KITEX','KKCL','KLRF','KMSUGAR','KNRCON','KOHINOOR','KOKUYOCMLN','KOLTEPATIL','KOPRAN','KOTAKBANK','KOTARISUG','KOTHARIPET','KOTHARIPRO','KOVAI','KPIT','KPRMILL','KRBL','KRISHNAENG','KRITIIND','KSBPUMPS','KSCL','KSE','KSERASERA','KSK','KSL','KTIL','KTKBANK','KWALITY','L&TFH','LAKPRE','LAKSHMIEFL','LAKSHMIFIN','LAKSHMIMIL','LAKSHVILAS','LANCOIN','LAOPALA','LAXMIMACH','LCCINFOTEC','LGBBROSLTD','LGBFORGE','LIBERTSHOE','LICHSGFIN','LINDEINDIA','LITL','LLOYDELENG','LLOYDFIN','LML','LOGIXMICRO','LOKESHMACH','LOTUSEYE','LOVABLE','LPDC','LT','LUMAXAUTO','LUMAXIND','LUMAXTECH','LUPIN','LUPIN','LYKALABS','LYPSAGEMS','M&M','M&MFIN','MAANALU','MADHAV','MADHUCON','MADRASFERT','MAGMA','MAGNUM','MAHABANK','MAHINDCIE','MAHINDUGIN','MAHLIFE','MAHSCOOTER','MAHSEAMLES','MAITHANALL','MALUPAPER','MALWACOTT','MANAKSIA','MANALIPETC','MANAPPURAM','MANDHANA','MANGALAM','MANGCHEFER','MANGLMCEM','MANGTIMBER','MANINDS','MANINFRA','MANJEERA','MANJUSHREE','MANUGRAPH','MARALOVER','MARG','MARICO','MARKSANS','MARUTI','MASTEK','MAWANASUG','MAX','MAXWELL','MAYURUNIQ','MBECL','MBLINFRA','MCDHOLDING','MCDOWELL-N','MCLEODRUSS','MCX','MCX','MEGASOFT','MEGH','MELSTAR','MERCATOR','MERCK','METKORE','MHRIL','MIC','MICROSEC','MICROTECH','MINDACORP','MINDAIND','MINDTREE','MIRCELECTR','MIRZAINT','MMFL','MMTC','MOHITIND','MOIL','MONNETISPA','MONSANTO','MORARJEE','MOREPENLAB','MOSERBAER','MOTHERSUMI','MOTILALOFS','MOTOGENFIN','MPHASIS','MPSLTD','MRF','MRO-TEK','MRPL','MSPL','MTEDUCARE','MTNL','MUDRA','MUKANDENGG','MUKANDLTD','MUKTAARTS','MUNJALAU','MUNJALSHOW','MURLIIND','MURUDCERA','MUTHOOTFIN','MVL','MVLIND','MYSOREBANK','NAGAROIL','NAGREEKCAP','NAGREEKEXP','NAHARCAP','NAHARINDUS','NAHARPOLY','NAHARSPING','NAKODA','NATCOPHARM','NATHBIOGEN','NATHSEED','NATIONALUM','NATNLSTEEL','NAUKRI','NAVINFLUOR','NAVNETEDUL','NBCC','NBVENTURES','NCC','NCLIND','NCOPPER','NDL','NDTV','NECLIFE','NEHAINT','NELCAST','NELCO','NEOCORP','NEPCMICON','NESCO','NESTLEIND','NET4','NETWORK18','NEULANDLAB','NEXTMEDIA','NEYVELILIG','NFL','NGCT','NHPC','NIBL','NIBL','NICCO','NIITLTD','NIITTECH','NILKAMAL','NIPPOBATRY','NITCO','NITESHEST','NITINFIRE','NITINSPIN','NMDC','NOCIL','NOIDATOLL','NOL','NORBTEAEXP','NOVOPANIND','NRBBEARING','NRC','NSIL','NTPC','NUCLEUS','NUTEK','OBEROIRLTY','OCL','OFSS','OIL','OILCOUNTUB','OISL','OMAXAUTO','OMAXE','OMKARCHEM','OMMETALS','OMNITECH','ONELIFECAP','ONGC','ONMOBILE','ONWARDTEC','OPTOCIRCUI','ORBITCORP','ORBTEXP','ORCHIDCHEM','ORIENTABRA','ORIENTALTL','ORIENTBANK','ORIENTBELL','ORIENTCEM','ORIENTHOT','ORIENTLTD','ORIENTPPR','ORIENTREF','ORISSAMINE','OSWALMIN','OUDHSUG','PAEL','PAGEIND','PALRED','PANACEABIO','PANAMAPET','PANASONIC','PANCARBON','PANCHSHEEL','PANORAMUNI','PAPERPROD','PARABDRUGS','PARACABLES','PARAL','PARAPRINT','PARASPETRO','PARRYSUGAR','PARSVNATH','PATELENG','PATINTLOG','PATSPINLTD','PBAINFRA','PCJEWELLER','PDPL','PDUMJEIND','PDUMJEPULP','PEARLPOLY','PEL','PENIND','PENIND','PENINLAND','PERIATEA','PERSISTENT','PETRONENGG','PETRONET','PFC','PFIZER','PFOCUS','PFRL','PFS','PGEL','PGHH','PGIL','PHILIPCARB','PHOENIXLL','PHOENIXLTD','PIDILITIND','PIIND','PILANIINV','PILIND','PIONDIST','PIONEEREMB','PIPAVAVDOC','PIRGLASS','PIRPHYTO','PITTILAM','PLASTIBLEN','PLETHICO','PNB','PNB','PNBGILTS','PNC','POCHIRAJU','POLARIS','POLYMED','POLYPLEX','PONDYOXIDE','PONNIERODE','POWERGRID','PPAP','PRADIP','PRAENG','PRAJIND','PRAKASH','PRAKASHCON','PRAKASHSTL','PRATIBHA','PRECOT','PRECWIRE','PREMIER','PREMIERPOL','PRESSMN','PRESTIGE','PRICOL','PRIMESECU','PRISMCEM','PRITHVI','PRITHVISOF','PROVOGE','PROZONECSC','PSB','PSL','PTC','PTL','PUNJABCHEM','PUNJLLOYD','PURVA','PVP','PVR','QUINTEGRA','RADAAN','RADICO','RAIN','RAINBOWPAP','RAIREKMOH','RAJESHEXPO','RAJOIL','RAJPALAYAM','RAJRAYON','RAJSREESUG','RAJTV','RAJVIR','RALLIS','RAMANEWS','RAMCOCEM','RAMCOIND','RAMCOSYS','RAMKY','RAMSARUP','RANASUG','RANBAXY','RANEENGINE','RANEHOLDIN','RANKLIN','RASOYPR','RATNAMANI','RAYMOND','RBL','RBN','RCF','RCOM','RECLTD','REDINGTON','REFEX','REGENCERAM','REIAGROLTD','REISIXTEN','RELAXO','RELCAPITAL','RELIANCE','RELIGARE','RELINFRA','RELMEDIA','REMSONSIND','RENUKA','REPCOHOME','REPRO','RESPONIND','REVATHI','RICOAUTO','RIIL','RJL','RKDL','RKFORGE','RMCL','RML','RMMIL','ROHITFERRO','ROHLTD','ROLTA','ROSSELLIND','RPGLIFE','RPOWER','RPPINFRA','RSSOFTWARE','RSWM','RSYSTEMS','RUBYMILLS','RUCHINFRA','RUCHIRA','RUCHISOYA','RUPA','RUSHIL','SABERORGAN','SABTN','SADBHAV','SAGCEM','SAHPETRO','SAIL','SAKHTISUG','SAKSOFT','SAKTHIFIN','SAKUMA','SALONACOT','SALORAINTL','SALSTEEL','SAMBANDAM','SAMBHAAV','SAMINDUS','SAMTEL','SANDESH','SANGAMIND','SANGHIIND','SANGHVIFOR','SANGHVIMOV','SANOFI','SANWARIA','SARDAEN','SAREGAMA','SARLAPOLY','SARTHAKIND','SASKEN','SATHAISPAT','SAVERA','SAYAJIHOTL','SBBJ','SBIN','SBT','SCHNEIDER','SCI','SEAMECLTD','SEINV','SELAN','SELMCL','SEPOWER','SERVALL','SESHAPAPER','SEZAL','SFCL','SGFL','SGJHL','SGL','SHAHALLOYS','SHAKTIPUMP','SHALPAINTS','SHANTIGEAR','SHARONBIO','SHARRESLTD','SHASUNPHAR','SHILPAMED','SHILPI','SHIRPUR-G','SHIV-VANI','SHIVAMAUTO','SHIVTEX','SHLAKSHMI','SHOPERSTOP','SHREEASHTA','SHREECEM','SHREERAMA','SHRENUJ','SHREYANIND','SHREYAS','SHRINATRAJ','SHRIRAMCIT','SHRIRAMEPC','SHYAMTEL','SICAGEN','SICAL','SIEMENS','SIGNETIND','SIL','SILINV','SIMBHSUGAR','SIMPLEX','SIMPLEXCAS','SIMPLEXINF','SINTEX','SIRPAPER','SITASHREE','SITICABLE','SIYSIL','SJVN','SKFINDIA','SKMEGGPROD','SKSMICRO','SKUMARSYNF','SMARTLINK','SMLISUZU','SMOBILITY','SMPL','SMSPHARMA','SOBHA','SOFTTECHGR','SOLARINDS','SOMANYCERA','SOMATEX','SONASTEER','SONATSOFTW','SOTL','SOUISPAT','SOUTHBANK','SPARC','SPECIALITY','SPECTACLE','SPENTEX','SPIC','SPLIL','SPMLINFRA','SPYL','SREEL','SREINFRA','SRF','SRGINFOTEC','SRHHYPOLTD','SRICHAMUND','SRSLTD','SRTRANSFIN','SSLT','SSWL','STAR','STAR','STARPAPER','STCINDIA','STEL','STERLINBIO','STERTOOLS','STINDIA','STOREONE','STRTECH','STYABS','SUBEX','SUBROS','SUDAR','SUDARSCHEM','SUJANATWR','SUJANAUNI','SUMEETINDS','SUMMITSEC','SUNCLAYLTD','SUNDARAM','SUNDARMFIN','SUNDRMBRAK','SUNDRMFAST','SUNFLAG','SUNILHITEC','SUNPHARMA','SUNTECK','SUNTV','SUPER','SUPERSPIN','SUPPETRO','SUPRAJIT','SUPREMEIND','SUPREMEINF','SUPREMETEX','SURANACORP','SURANAIND','SURANAT&P','SURANAVEL','SURYAJYOTI','SURYALAXMI','SURYAPHARM','SURYAROSNI','SUTLEJTEX','SUVEN','SUZLON','SWANENERGY','SWARAJENG','SWELECTES','SYMPHONY','SYNCOM','SYNDIBANK','TAINWALCHM','TAJGVK','TAKE','TALBROAUTO','TALWALKARS','TANFACIND','TANLA','TANTIACONS','TARAJEWELS','TARAPUR','TARMAT','TATACHEM','TATACHEM','TATACOFFEE','TATACOMM','TATAELXSI','TATAGLOBAL','TATAINVEST','TATAMETALI','TATAMOTORS','TATAMTRDVR','TATAPOWER','TATASPONGE','TATASTEEL','TBZ','TCI','TCIDEVELOP','TCIFINANCE','TCPLTD','TCS','TDPOWERSYS','TECHM','TECHNO','TECHNOFAB','TECPRO','TEXINFRA','TEXMOPIPES','TEXRAIL','TFCILTD','TFL','TGBHOTELS','THANGAMAYL','THEBYKE','THEMISMED','THERMAX','THINKSOFT','THIRUSUGAR','THOMASCOOK','THOMASCOTT','TI','TIDEWATER','TIIL','TIJARIA','TIL','TIMBOR','TIMESGTY','TIMETECHNO','TIMKEN','TINPLATE','TIPSINDLTD','TIRUMALCHM','TITAN','TITAN','TNPETRO','TNPL','TNTELE','TODAYS','TOKYOPLAST','TORNTPHARM','TORNTPOWER','TREEHOUSE','TRENT','TRF','TRICOM','TRIDENT','TRIGYN','TRIL','TRITURBINE','TRIVENI','TTKHEALTH','TTKPRESTIG','TTL','TTML','TUBEINVEST','TULIP','TULSI','TULSYAN','TV18BRDCST','TVSELECT','TVSMOTOR','TVSSRICHAK','TVTODAY','TWILITAKA','TWL','UBENGG','UBHOLDINGS','UBL','UCALFUEL','UCOBANK','UFLEX','UGARSUGAR','UJAAS','ULTRACEMCO','UMESLTD','UNICHEMLAB','UNIENTER','UNIONBANK','UNIPLY','UNITECH','UNITEDBNK','UNITEDTEA','UNITY','UNIVCABLES','UPERGANGES','UPL','USHAMART','USHERAGRO','UTTAMSTL','UTTAMSUGAR','UTTAMVALUE','V2RETAIL','VADILALIND','VAIBHAVGBL','VAKRANGEE','VALECHAENG','VALUEIND','VARDHACRLC','VARDMNPOLY','VARUN','VARUNSHIP','VASCONEQ','VASWANI','VENKEYS','VENUSREM','VESUVIUS','VGUARD','VHL','VICEROY','VIDEOIND','VIJAYABANK','VIJSHAN','VIKASGLOB','VIMALOIL','VIMTALABS','VINATIORGA','VINDHYATEL','VINYLINDIA','VIPIND','VIPUL','VISAKAIND','VISASTEEL','VISESHINFO','VISUINTL','VIVIDHA','VIVIMEDLAB','VKSPL','VLSFINANCE','VMART','VOLTAMP','VOLTAMP','VOLTAS','VSSL','VSTIND','VSTTILLERS','VTL','VTMLTD','VTXIND','WABAG','WABCOINDIA','WALCHANNAG','WANBURY','WEBELSOLAR','WEIZFOREX','WEIZMANIND','WELCORP','WELINV','WELPROJ','WELSPUNIND','WENDT','WHEELS','WHIRLPOOL','WILLAMAGOR','WINDMACHIN','WINSOME','WINSOMEDJ','WIPRO','WOCKPHARMA','WSI','WSTCSTPAPR','WYETH','XCHANGING','XLENERGY','XPROINDIA','YESBANK','ZANDUREALT','ZEEL','ZEELEARN','ZEEMEDIA','ZENITHBIR','ZENITHCOMP','ZENITHEXPO','ZENITHINFO','ZENSARTECH','ZICOM','ZODIACLOTH','ZODJRDMKJ','ZUARI','ZUARIGLOB','ZYDUSWELL','ZYLOG'
];

var urlPre='http://www.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=';
var urlPost='&segmentLink=3&symbolCount=2&series=ALL&dateRange=3month&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE';

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
      casper.thenOpen(urlPre+symbolList[i]+urlPost, function() {  
          casper.download('http://www.nseindia.com/'+this.evaluate(getLink),symbolList[j]+'.csv'); 
          j++;
		});     
	}
});

casper.then(function() {
	casper.echo('Done');
});
casper.run();