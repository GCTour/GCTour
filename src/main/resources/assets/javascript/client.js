let map;
let testicon

$(document).ready(function () {
    map = initMap();
    map.on('moveend', function () {
        //alert(map.getBounds().getNorth() + map.getBounds().getEast() + map.getBounds().getSouth() + map.getBounds().getWest())
    });
});

function initMap() {

    let osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
    });

    let googleMaps = L.tileLayer("http://mt.google.com/vt?x={x}&y={y}&z={z}", {
        attribution: "Google Maps",
        tileSize: 256,
        minZoom: 0,
        maxZoom: 20
    });

    let googleMapsSatellite = L.tileLayer("http://mt.google.com/vt?lyrs=s&x={x}&y={y}&z={z}", {
        attribution: "Google Maps",
        tileSize: 256,
        minZoom: 3,
        maxZoom: 20
    });

    let googleMapsHybrid = L.tileLayer("http://mt0.google.com/vt/lyrs=s,m@110&hl=en&x={x}&y={y}&z={z}", {
        attribution: "Google Maps",
        tileSize: 256,
        minZoom: 0,
        maxZoom: 20
    });

    //TODO implement more map layers and refactor/simplify this
    let map_layers = {
        "Geocaching": {
            tileUrl: "https://maptiles{s}.geocaching.com/tile/{z}/{x}/{y}.png?token={accessToken}",
            accessToken: 'asdasdasdsa',
            subdomains: ['01', '02', '03', '04', '05', '06', '07', '08'],
            minZoom: 0,
            maxZoom: 18
        }
        ,
        "OpenStreetMap Default": {
            tileUrl: "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
            active: true
        }
        ,
        "OpenStreetMap German Style": {
            tileUrl: "http://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
        }
        ,
        "OpenStreetMap Black and White": {
            tileUrl: "http://{s}.www.toolserver.org/tiles/bw-mapnik/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
        }
        ,
        "OpenStreetMap Hike and Bike": {
            tileUrl: "http://toolserver.org/tiles/hikebike/{z}/{x}/{y}.png",
            attribution: 'Map and map data \u00a9 2012 <a href="http://www.openstreetmap.org" target=\'_blank\'>OpenStreetMap</a> and contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>.',
            tileSize: 256,
            minZoom: 0,
            maxZoom: 20
        }
        ,
        "Thunderforest OpenCycleMap": {
            tileUrl: "http://{s}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
        }
        ,
        "Thunderforest Transport": {
            tileUrl: "http://{s}.tile2.opencyclemap.org/transport/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
        }
        ,
        "Thunderforest Landscape": {
            tileUrl: "http://{s}.tile3.opencyclemap.org/landscape/{z}/{x}/{y}.png",
            attribution: '&copy; <a href="http://www.opencyclemap.org">OpenCycleMap</a>, <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
        }
        ,
        "Stamen Toner": {
            tileUrl: "http://{s}.tile.stamen.com/toner/{z}/{x}/{y}.png",
            attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>',
            subdomains: "abcd",
            minZoom: 0,
            maxZoom: 20
        }
        ,
        "Stamen Terrain": {
            tileUrl: "http://{s}.tile.stamen.com/terrain/{z}/{x}/{y}.png",
            attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>',
            subdomains: "abcd",
            minZoom: 4,
            maxZoom: 18
        }
        ,
        "Stamen Watercolor": {
            tileUrl: "http://{s}.tile.stamen.com/watercolor/{z}/{x}/{y}.png",
            attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://creativecommons.org/licenses/by-sa/3.0">CC BY SA</a>',
            subdomains: "abcd",
            minZoom: 3,
            maxZoom: 16
        }
        ,
        "Esri WorldStreetMap": {
            tileUrl: "http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri"
        }
        ,
        "Esri DeLorme": {
            tileUrl: "http://server.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri &mdash; Copyright: \u00a92012 DeLorme",
            maxZoom: 11
        }
        ,
        "Esri WorldTopoMap": {
            tileUrl: "http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase, Kadaster NL, Ordnance Survey, Esri Japan, METI, Esri China (Hong Kong), and the GIS User Community"
        }
        ,
        "Esri WorldImagery": {
            tileUrl: "http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community"
        }
        ,
        "Esri OceanBasemap": {
            tileUrl: "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri &mdash; Sources: GEBCO, NOAA, CHS, OSU, UNH, CSUMB, National Geographic, DeLorme, NAVTEQ, and Esri",
            maxZoom: 11
        }
        ,
        "Esri NatGeoWorldMap": {
            tileUrl: "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}",
            attribution: "Tiles &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC"
        }
        ,
        "Google Maps": {
            tileUrl: "http://mt.google.com/vt?x={x}&y={y}&z={z}",
            attribution: "Google Maps",
            tileSize: 256,
            minZoom: 0,
            maxZoom: 20
        }
        ,
        "Google Maps Satellite": {
            tileUrl: "http://mt.google.com/vt?lyrs=s&x={x}&y={y}&z={z}",
            attribution: "Google Maps",
            tileSize: 256,
            minZoom: 3,
            maxZoom: 20
        }
        ,
        "Google Maps Hybrid": {
            tileUrl: "http://mt0.google.com/vt/lyrs=s,m@110&hl=en&x={x}&y={y}&z={z}",
            attribution: "Google Maps",
            tileSize: 256,
            minZoom: 0,
            maxZoom: 20
        }
    };

    let hillshadow = L.tileLayer("http://{s}.tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png", {
        attribution: 'hillshadow \u00a9 <a href="http://tiles.wmflabs.org/" target=\'_blank\'>tiles.wmflabs.org</a>',
        tileSize: 256,
        minZoom: 0,
        maxZoom: 17
    });


    let baseLayers = {
        "OpenStreetMap": osm,
        "Google Maps": googleMaps,
        "Google Maps Satellite": googleMapsSatellite,
        "Google Maps Hybrid": googleMapsHybrid
    };
    let overlayMaps = {
        "Hillshadow": hillshadow,
    };

    let map = L.map('map', {
        zoom: 12,
        center: [48.8, 9.3],
        layers: [osm],
        scrollWheelZoom: true,
        dragging: true,
        zoomControl: true,
        tap: false //disable moving on map for touch to make scrolling on site available
    });

    L.control.layers(baseLayers, overlayMaps).addTo(map);
    return map;
}