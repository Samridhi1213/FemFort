// Global State
const state = {
    zones: [],
    currentZone: null,
    map: null,
    routeLayer: null,
    userMarker: null,
    campusId: null,
    requestMap: null,
    requestMarker: null,
    campuses: [] // NEW: keep full campus list with centers
};

// API Service
const api = {
    getCampuses: async () => {
        const res = await fetch('api/campuses');
        return await res.json();
    },
    getZones: async (campusId) => {
        const res = await fetch(`api/zones?campusId=${campusId}`);
        return await res.json();
    },
    getRoute: async (fromId, toId, campusId) => {
        const res = await fetch(`api/routes?fromZoneId=${fromId}&toZoneId=${toId}&campusId=${campusId}`);
        return await res.json();
    },
    submitRating: async (data) => {
        const res = await fetch('api/ratings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res;
    },
    submitRequest: async (data) => {
        const res = await fetch('api/requests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return await res.json();
    }
};

// Map Logic
const mapLogic = {
    init: (elementId, center) => {
        if (state.map) state.map.remove();

        state.map = L.map(elementId).setView(center, 16);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(state.map);

        // Map Click Listener for Requesting Locations
        state.map.on('click', (e) => {
            ui.openRequestModal(e.latlng.lat, e.latlng.lng);
        });
    },

    initRequestMap: (lat, lng) => {
        if (state.requestMap) state.requestMap.remove();

        state.requestMap = L.map('request-map').setView([lat, lng], 17);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(state.requestMap);

        // Draggable Marker
        state.requestMarker = L.marker([lat, lng], { draggable: true }).addTo(state.requestMap);

        state.requestMarker.on('dragend', (e) => {
            const pos = e.target.getLatLng();
            const latInput = document.getElementById('new-zone-lat');
            const lngInput = document.getElementById('new-zone-lng');
            if (latInput && lngInput) {
                latInput.value = pos.lat;
                lngInput.value = pos.lng;
            }
        });
    },

    renderZones: (zones) => {
        // Clear existing non-tile layers
        state.map.eachLayer(layer => {
            if (layer instanceof L.Circle || layer instanceof L.Polygon ||
                layer instanceof L.Rectangle || layer instanceof L.Marker) {
                if (!layer._url) state.map.removeLayer(layer);
            }
        });

        zones.forEach(zone => {
            // Dynamic Radius Calculation
            let dynamicRadius = zone.radiusMeters || 100;
            const minRadius = 20;

            let minDistance = Infinity;
            zones.forEach(other => {
                if (zone.id !== other.id) {
                    const dist = L.latLng(zone.lat, zone.lng).distanceTo(L.latLng(other.lat, other.lng));
                    if (dist < minDistance) minDistance = dist;
                }
            });

            if (minDistance !== Infinity) {
                const maxAllowed = (minDistance / 2) * 0.9;
                dynamicRadius = Math.min(dynamicRadius, maxAllowed);
            }
            dynamicRadius = Math.max(dynamicRadius, minRadius);

            let color;
            if (zone.currentSafetyScore === 0.0 && zone.ratingCount === 0) {
                color = '#808080'; // Grey for unrated
            } else {
                color = mapLogic.getSafetyColor(zone.currentSafetyScore);
            }
            const threatType = zone.dominantThreatType ? zone.dominantThreatType.toLowerCase() : 'overcrowded';
            let shapeLayer;

            if (threatType.includes('construction')) {
                // Square (Rectangle)
                const rDeg = dynamicRadius / 111000;
                const bounds = [
                    [zone.lat - rDeg, zone.lng - rDeg],
                    [zone.lat + rDeg, zone.lng + rDeg]
                ];
                shapeLayer = L.rectangle(bounds, {
                    color,
                    fillColor: color,
                    fillOpacity: 0.4
                });
            } else if (threatType.includes('lighting')) {
                // Triangle (Polygon)
                const rDeg = dynamicRadius / 111000;
                const p1 = [zone.lat + rDeg, zone.lng];
                const p2 = [zone.lat - rDeg / 2, zone.lng - rDeg];
                const p3 = [zone.lat - rDeg / 2, zone.lng + rDeg];

                shapeLayer = L.polygon([p1, p2, p3], {
                    color,
                    fillColor: color,
                    fillOpacity: 0.4
                });
            } else {
                // Default: Circle
                shapeLayer = L.circle([zone.lat, zone.lng], {
                    color,
                    fillColor: color,
                    fillOpacity: 0.4,
                    radius: dynamicRadius
                });
            }

            shapeLayer.addTo(state.map)
                .bindPopup(`
                  <b>${zone.name}</b><br>
                  Severity Level: ${zone.currentSafetyScore.toFixed(1)}/5<br>
                  Threat: ${zone.dominantThreatType || 'None'}<br>
                  <button onclick="ui.openRateModal(${zone.id})" class="btn btn-sm mt-2">Rate Area</button>
              `);
        });
    },

    renderRoute: (zones) => {
        if (state.routeLayer) state.map.removeLayer(state.routeLayer);

        const latlngs = zones.map(z => [z.lat, z.lng]);
        state.routeLayer = L.polyline(latlngs, { color: 'blue', weight: 5 }).addTo(state.map);
        state.map.fitBounds(state.routeLayer.getBounds());
    },

    getSafetyColor: (score) => {
        if (score <= 1.5) return '#2ECC71'; // Low Risk
        if (score <= 2.5) return '#A3E635'; // Minor Risk
        if (score <= 3.5) return '#F4A100'; // Moderate Risk
        if (score <= 4.5) return '#FF5C33'; // High Risk
        return '#C30000'; // Severe Risk
    }
};

// UI Logic
const ui = {
    init: async () => {
        // Fetch campuses and populate modal + store in state
        const campuses = await api.getCampuses();
        state.campuses = campuses || [];

        const select = document.getElementById('campus-select');
        if (select) {
            select.innerHTML = '';
            state.campuses.forEach(c => {
                const option = document.createElement('option');
                option.value = c.id;
                const name = c.name || c.campusName || `Campus ${c.id}`;
                // support both camelCase and snake_case from backend
                const centerLat = c.centerLat ?? c.center_lat ?? c.lat ?? c.latitude;
                const centerLng = c.centerLng ?? c.center_lng ?? c.lng ?? c.longitude;
                option.text = name;
                if (centerLat != null && centerLng != null) {
                    option.dataset.lat = centerLat;
                    option.dataset.lng = centerLng;
                }
                select.appendChild(option);
            });

            // Show modal
            const modal = document.getElementById('campus-select-modal');
            if (modal) modal.style.display = 'flex';
        }
    },

    populateSelects: (zones) => {
        const fromSelect = document.getElementById('fromZone');
        const toSelect = document.getElementById('toZone');
        if (!fromSelect || !toSelect) return;

        fromSelect.innerHTML = '<option value="">Select Start</option>';
        toSelect.innerHTML = '<option value="">Select Destination</option>';

        zones.forEach(z => {
            const option = `<option value="${z.id}">${z.name}</option>`;
            fromSelect.insertAdjacentHTML('beforeend', option);
            toSelect.insertAdjacentHTML('beforeend', option);
        });
    },

    handleRouteSubmit: async (e) => {
        e.preventDefault();
        const fromId = document.getElementById('fromZone').value;
        const toId = document.getElementById('toZone').value;

        if (!state.campusId) {
            alert("Please select a campus first.");
            return;
        }

        const route = await api.getRoute(fromId, toId, state.campusId);
        if (route.length > 0) {
            mapLogic.renderRoute(route);
            document.getElementById('route-info').innerHTML = `
                <div class="card">
                    <h3>Route Found</h3>
                    <p>Via: ${route.map(z => z.name).join(' → ')}</p>
                </div>
            `;
        } else {
            alert('No safe route found!');
        }
    },

    handleCampusSelect: () => {
        const select = document.getElementById('campus-select');
        if (!select || select.selectedIndex === -1) {
            alert("Please select a campus.");
            return;
        }

        const selectedOption = select.options[select.selectedIndex];
        state.campusId = selectedOption.value;

        let lat = parseFloat(selectedOption.dataset.lat);
        let lng = parseFloat(selectedOption.dataset.lng);

        if (Number.isNaN(lat) || Number.isNaN(lng)) {
            // Fallback: Galgotias default if coords missing
            lat = 28.365850;
            lng = 77.541227;
        }

        mapLogic.init('map', [lat, lng]);

        const modal = document.getElementById('campus-select-modal');
        if (modal) modal.style.display = 'none';

        api.getZones(state.campusId).then(zones => {
            state.zones = zones;
            mapLogic.renderZones(state.zones);
            ui.populateSelects(state.zones);
        });
    },

    openRateModal: (zoneId) => {
        document.getElementById('rate-zone-id').value = zoneId;
        document.getElementById('rate-zone-modal').classList.remove('hidden');
    },

    handleRateSubmit: async (e) => {
        e.preventDefault();
        const zoneId = document.getElementById('rate-zone-id').value;
        const threat = document.getElementById('rate-zone-threat').value;
        const severity = document.getElementById('rate-zone-severity').value;
        const comment = document.getElementById('rate-zone-comment').value;

        try {
            const res = await api.submitRating({
                zoneId: parseInt(zoneId),
                score: 0, // Deprecated
                threatCategoryId: parseInt(threat),
                severityLevel: parseInt(severity),
                comment: comment,
                userId: 1
            });

            if (res.ok) {
                alert('Rating submitted! Thank you.');
                document.getElementById('rate-zone-modal').classList.add('hidden');
                document.getElementById('rate-zone-form').reset();
                // Refresh zones
                state.zones = await api.getZones(state.campusId);
                mapLogic.renderZones(state.zones);
            } else {
                alert('Error submitting rating.');
            }
        } catch (err) {
            console.error(err);
            alert('Error submitting rating.');
        }
    },

    // CLICK → decide ZONE vs CAMPUS based on 3km distance from nearest campus
    openRequestModal: (lat, lng) => {
        const latInput = document.getElementById('new-zone-lat');
        const lngInput = document.getElementById('new-zone-lng');
        if (latInput && lngInput) {
            latInput.value = lat;
            lngInput.value = lng;
        }

        let requestType = 'CAMPUS';
        let nearestCampus = null;
        let nearestDist = Infinity;

        if (state.campuses && state.campuses.length > 0) {
            const clickPoint = L.latLng(lat, lng);

            state.campuses.forEach(c => {
                const cLat = c.centerLat ?? c.center_lat ?? c.lat ?? c.latitude;
                const cLng = c.centerLng ?? c.center_lng ?? c.lng ?? c.longitude;
                if (cLat == null || cLng == null) return;

                const dist = clickPoint.distanceTo(L.latLng(cLat, cLng)); // meters
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCampus = c;
                }
            });

            if (nearestCampus && nearestDist <= 3000) {
                // within 3km → treat as ZONE request for that campus
                requestType = 'ZONE';
                state.campusId = nearestCampus.id;
            } else {
                // farther than 3km from any known campus → new CAMPUS request
                requestType = 'CAMPUS';
            }
        }

        const typeSelect = document.getElementById('request-type');
        if (typeSelect) typeSelect.value = requestType;

        const titleEl = document.getElementById('request-modal-title');
        const descEl = document.getElementById('request-modal-desc');
        if (titleEl) {
            titleEl.textContent = requestType === 'ZONE'
                ? 'Request New Zone'
                : 'Request New Campus';
        }
        if (descEl) {
            descEl.textContent = `Drag the pin to adjust the exact location for this ${requestType.toLowerCase()}.`;
        }

        const modal = document.getElementById('create-zone-modal');
        if (modal) modal.classList.remove('hidden');

        // Update "coords display" field if present
        const displayInput = document.getElementById('request-coords-display');
        if (displayInput) {
            displayInput.value = `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
        }

        // Initialize mini-map after modal is visible
        setTimeout(() => {
            if (document.getElementById('request-map')) {
                mapLogic.initRequestMap(lat, lng);
            }
        }, 200);
    },

    handleRequestSubmit: async (e) => {
        e.preventDefault();
        const name = document.getElementById('new-zone-name').value;
        const desc = document.getElementById('new-zone-desc').value;
        const lat = parseFloat(document.getElementById('new-zone-lat').value);
        const lng = parseFloat(document.getElementById('new-zone-lng').value);
        const comment = document.getElementById('new-zone-comment').value;
        const type = document.getElementById('request-type').value;

        if (type === 'ZONE' && !state.campusId) {
            alert("Could not determine campus for this zone. Please click closer to an existing campus or try again.");
            return;
        }

        try {
            const res = await api.submitRequest({
                requestType: type,
                campusId: type === 'ZONE' ? parseInt(state.campusId) : null,
                name: name,
                description: desc,
                lat: lat,
                lng: lng,
                userComment: comment
            });

            if (res.message || res.id || res.status) {
                alert('Request submitted successfully! An admin will review it.');
                const modal = document.getElementById('create-zone-modal');
                if (modal) modal.classList.add('hidden');
                const form = document.getElementById('create-zone-form');
                if (form) form.reset();
            } else {
                alert('Failed to submit request: ' + (res.error || 'Unknown error'));
            }
        } catch (err) {
            console.error(err);
            alert('Error submitting request.');
        }
    },

    locateUser: () => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition((pos) => {
                const { latitude, longitude } = pos.coords;
                if (state.map) {
                    state.map.setView([latitude, longitude], 16);
                    if (state.userMarker) {
                        state.userMarker.setLatLng([latitude, longitude]);
                    } else {
                        state.userMarker = L.marker([latitude, longitude], {
                            icon: L.divIcon({
                                className: 'user-marker',
                                html: '<div style="background-color: #0984e3; width: 15px; height: 15px; border-radius: 50%; border: 2px solid white; box-shadow: 0 0 10px rgba(0,0,0,0.3);"></div>',
                                iconSize: [20, 20]
                            })
                        }).addTo(state.map).bindPopup("You are here");
                    }
                }
            }, (err) => {
                console.error("Geolocation error:", err);
                alert("Could not get your location. Please enable GPS.");
            });
        } else {
            alert("Geolocation is not supported by your browser.");
        }
    }
};

// Init
document.addEventListener('DOMContentLoaded', async () => {
    if (document.getElementById('map')) {
        ui.init(); // Show campus selection

        const routeForm = document.getElementById('route-form');
        if (routeForm) routeForm.addEventListener('submit', ui.handleRouteSubmit);

        const createZoneForm = document.getElementById('create-zone-form');
        if (createZoneForm) createZoneForm.addEventListener('submit', ui.handleRequestSubmit);

        const rateZoneForm = document.getElementById('rate-zone-form');
        if (rateZoneForm) rateZoneForm.addEventListener('submit', ui.handleRateSubmit);

        const locateBtn = document.getElementById('locate-btn');
        if (locateBtn) locateBtn.addEventListener('click', ui.locateUser);

        const campusConfirmBtn = document.getElementById('campus-confirm-btn');
        if (campusConfirmBtn) campusConfirmBtn.addEventListener('click', ui.handleCampusSelect);
    }
});
