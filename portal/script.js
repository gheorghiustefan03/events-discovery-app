let map;
let marker;
let images = []; // common: {type}, new {file}, existing {url, active}
let token = '';
let sortAscending = true;

function toggleSort() {
    sortAscending = !sortAscending;
    document.getElementById("sortButton").textContent = `Sort by Start Date ${sortAscending ? '↑' : '↓'}`;
    loadData(sortAscending);
}


document.addEventListener("DOMContentLoaded", () => {
    const fileInput = document.getElementById("fileInput");
    document.getElementById("g_id_onload").setAttribute("data-client_id", CONFIG.GOOGLE_CLIENT_ID);

    map = L.map('map').setView([51.505, -0.09], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    map.on('click', function (e) {
        const { lat, lng } = e.latlng;

        document.getElementById("locLat").value = lat.toFixed(6);
        document.getElementById("locLng").value = lng.toFixed(6);

        if (!marker) {
            marker = L.marker([lat, lng]).addTo(map);
        } else {
            marker.setLatLng([lat, lng]);
        }
    });

    document.getElementById("fileInput").addEventListener("change", function (e) {
        const files = Array.from(e.target.files);
        const validImages = files.filter(file => file.type.startsWith("image/"));

        validImages.forEach(file => {
            images.push({ type: "new", file });
        });

        renderAllImages();
        e.target.value = "";
    });

    fileInput.addEventListener("change", (e) => {
        const files = Array.from(e.target.files);
        files.forEach(file => {
            images.push({ type: 'new', file });
        });
        renderAllImages();
        fileInput.value = "";
    });

    document.getElementById("locationForm").addEventListener("submit", async e => {
        e.preventDefault();
        const id = document.getElementById("locationId").value;
        const body = JSON.stringify({
            name: document.getElementById("locName").value,
            latitude: parseFloat(document.getElementById("locLat").value),
            longitude: parseFloat(document.getElementById("locLng").value),
        });
        const res = await fetch(`http://localhost:5073/api/Location${id ? `/${id}` : ''}`, {
            method: id ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            },
            body
        });
        const data = await res.json();
        if (data.error) showErrors(data.error);
        else {
            resetForms();
            loadData();
        }
    });

    document.getElementById("eventForm").addEventListener("submit", async e => {
        e.preventDefault();

        const id = document.getElementById("eventId").value;
        const formData = new FormData();

        formData.append("name", document.getElementById("eventName").value);
        formData.append("description", document.getElementById("eventDescription").value);
        formData.append("locationId", document.getElementById("eventLocation").value);
        formData.append("link", document.getElementById("eventLink").value);
        formData.append("startDate", document.getElementById("eventStart").value);
        formData.append("endDate", document.getElementById("eventEnd").value);

        const cats = Array.from(document.getElementById("eventCategory").selectedOptions).map(o => o.value);
        cats.forEach(cat => formData.append("categories", cat));
        images.filter(nf => nf.type === 'new').forEach(nf => formData.append("files", nf.file));

        const existingImagesOrder = images.filter(img => img.type === 'existing' && img.active).map(img => ({ index: images.indexOf(img), id: Number(getFileNameWithoutExtension(img.url)) }));

        formData.append("existingImageOrder", JSON.stringify(existingImagesOrder));

        const res = await fetch(`http://localhost:5073/api/Event${id ? `/${id}` : ''}`, {
            method: id ? 'PUT' : 'POST',
            headers: { Authorization: `Bearer ${token}` },
            body: formData
        });

        const data = await res.json();
        if (data.error) showErrors(data.error);
        else {
            resetForms();
            loadData();
        }
    });
});

window.addEventListener("DOMContentLoaded", () => {

    const savedToken = localStorage.getItem("idToken");
    const expiresAt = parseInt(localStorage.getItem("tokenExpiresAt") || "0");

    if (savedToken && Date.now() < expiresAt) {
        const payload = JSON.parse(atob(savedToken.split('.')[1]));
        if (payload.email === CONFIG.ALLOWED_EMAIL) {
            token = savedToken;
            document.getElementById("auth").classList.add("hidden");
            document.getElementById("app").classList.remove("hidden");
            loadData();
        }
    }
});

async function loadData(sortAsc = true) {
    const locRes = await fetch('http://localhost:5073/api/Location?userLat=0&userLon=0&radius=20000000', {
        headers: { Authorization: `Bearer ${token}` }
    });
    const locs = await locRes.json();
    const locList = document.getElementById("locationList");
    const locSelect = document.getElementById("eventLocation");
    locList.innerHTML = locSelect.innerHTML = '';
    
    locs.forEach(loc => {
        locList.innerHTML += `<li class="list-group-item">${loc.name}
            <button onclick="editLocation(${loc.id})" class="btn btn-sm btn-warning float-end ms-2">Edit</button>
            <button onclick="deleteLocation(${loc.id})" class="btn btn-sm btn-danger float-end">Delete</button>
        </li>`;
        locSelect.innerHTML += `<option value="${loc.id}">${loc.name}</option>`;
    });

    const locIds = locs.map(l => l.id);
    let eventReqParams = "?";
    for (let locId of locIds) {
        eventReqParams += "lId=" + locId + "&";
    }
    if (eventReqParams.endsWith("&")) {
        eventReqParams = eventReqParams.slice(0, -1);
    }

    const eventReq = 'http://localhost:5073/api/Event' + (eventReqParams.length > 1 ? eventReqParams : "");
    const eventRes = await fetch(eventReq, {
        headers: { Authorization: `Bearer ${token}` }
    });
    const events = await eventRes.json();

    events.sort((a, b) => {
        const dateA = new Date(a.startDate);
        const dateB = new Date(b.startDate);
        return sortAsc ? dateA - dateB : dateB - dateA;
    });

    const eventList = document.getElementById("eventList");
    eventList.innerHTML = '';
    events.forEach(ev => {
        const eventDate = new Date(ev.startDate);
        const startDateStr = eventDate.toLocaleString();
        const isPast = eventDate < new Date();

        // Add 'text-muted' and reduce opacity if event is in the past
        const listItemClass = isPast ? 'list-group-item text-muted' : 'list-group-item';

        eventList.innerHTML += `<li class="${listItemClass} d-flex justify-content-between align-items-center" style="${isPast ? 'opacity: 0.6;' : ''}">
            <div>
                <strong>${ev.name}</strong><br>
                <small class="text-muted">Starts: ${startDateStr}</small>
            </div>
            <div>
                <button onclick="editEvent(${ev.id})" class="btn btn-sm btn-warning me-2">Edit</button>
                <button onclick="deleteEvent(${ev.id})" class="btn btn-sm btn-danger">Delete</button>
            </div>
        </li>`;
    });

}



function getFileNameWithoutExtension(urlOrPath) {
    const cleanUrl = urlOrPath.replace(/\\/g, '/').split(/[?#]/)[0];
    const fileName = cleanUrl.split('/').pop();
    return fileName.split('.').slice(0, -1).join('.') || fileName;
}

function showErrors(errors) {
    const box = document.getElementById("errorBox");
    box.classList.remove("hidden");
    box.innerHTML = errors.map(e => `<div>${e}</div>`).join('');
    setTimeout(() => { box.innerHTML = null; box.classList.add("hidden") }, 5000);
}

function resetForms() {
    document.getElementById("locationForm").reset();
    document.getElementById("eventForm").reset();
    document.getElementById("locationId").value = '';
    document.getElementById("eventId").value = '';
    images = [];
    renderAllImages();
}

function renderAllImages() {
    let coverChosen = false;
    const container = document.getElementById("imagePreviewContainer");
    container.innerHTML = "";

    function handleDragStart(e) {
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', JSON.stringify({
            type: e.currentTarget.dataset.type,
            index: parseInt(e.currentTarget.dataset.index)
        }));
        e.currentTarget.classList.add('dragging');
    }

    function handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        e.currentTarget.classList.add('drag-over');
    }

    function handleDragLeave(e) {
        e.currentTarget.classList.remove('drag-over');
    }

    function handleDrop(e) {
        e.preventDefault();
        e.currentTarget.classList.remove('drag-over');

        const data = JSON.parse(e.dataTransfer.getData('text/plain'));
        const fromIndex = data.index;
        const toIndex = parseInt(e.currentTarget.dataset.index);

        const temp = images[fromIndex];
        images[fromIndex] = images[toIndex];
        images[toIndex] = temp;

        renderAllImages();
    }

    function handleDragEnd(e) {
        e.currentTarget.classList.remove('dragging');
        container.querySelectorAll('.image-thumb').forEach(el => el.classList.remove('drag-over'));
    }

    images.forEach((img, i) => {
        const div = document.createElement("div");
        div.setAttribute('draggable', 'true');
        div.dataset.type = img.type;
        div.dataset.index = i;
        div.className = 'image-thumb' + ((img.active || img.type === 'new') ? '' : ' inactive');
        if (!coverChosen && (img.type === 'new' || img.active === true)) {
            div.classList.add('cover');
            coverChosen = true;
        }


        div.addEventListener('dragstart', handleDragStart);
        div.addEventListener('dragover', handleDragOver);
        div.addEventListener('dragleave', handleDragLeave);
        div.addEventListener('drop', handleDrop);
        div.addEventListener('dragend', handleDragEnd);

        const image = document.createElement("img");
        if (img.type === 'existing') { image.src = img.url; }
        else {
            const reader = new FileReader();
            reader.onload = (e) => {
                image.src = e.target.result;
            }
            reader.readAsDataURL(img.file);
        }


        div.appendChild(image);

        const btn = document.createElement("button");
        btn.className = "toggle-active";
        btn.innerHTML = (img.active || img.type === 'new') ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        btn.addEventListener("click", () => {
            if (img.type === 'existing') img.active = !img.active;
            else images.splice(i, 1);
            renderAllImages();
        });
        div.appendChild(btn);

        container.appendChild(div);
    });
}

function editLocation(id) {
    showLocationForm();

    fetch(`http://localhost:5073/api/Location/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
    })
        .then(res => res.json())
        .then(loc => {
            document.getElementById("locationId").value = loc.id;
            document.getElementById("locName").value = loc.name;

            document.getElementById("locLat").value = loc.latitude;
            document.getElementById("locLng").value = loc.longitude;

            setTimeout(() => {
                map.invalidateSize();

                map.setView([loc.latitude, loc.longitude], 15);

                if (!marker) {
                    marker = L.marker([loc.latitude, loc.longitude]).addTo(map);
                } else {
                    marker.setLatLng([loc.latitude, loc.longitude]);
                }
            }, 100);
        });
}

function deleteLocation(id) {
    if (confirm("Are you sure you want to delete this location?")) {
        fetch(`http://localhost:5073/api/Location/${id}`, {
            method: "DELETE",
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(() => loadData());
    }
}

function editEvent(id) {
    showEventForm();
    images = [];
    fetch(`http://localhost:5073/api/Event/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
    })
        .then(res => res.json())
        .then(ev => {
            document.getElementById("eventId").value = ev.id;
            document.getElementById("eventName").value = ev.name;
            document.getElementById("eventDescription").value = ev.description;
            document.getElementById("eventLocation").value = ev.locationId;
            document.getElementById("eventLink").value = ev.link;
            document.getElementById("eventStart").value = ev.startDate?.slice(0, 16);
            document.getElementById("eventEnd").value = ev.endDate?.slice(0, 16);

            const categorySelect = document.getElementById("eventCategory");
            Array.from(categorySelect.options).forEach(opt => {
                opt.selected = ev.categories.includes(parseInt(opt.value));
            });

            loadExistingEventImages(ev.imageUrls);
        });
}

function deleteEvent(id) {
    if (confirm("Are you sure you want to delete this event?")) {
        fetch(`http://localhost:5073/api/Event/${id}`, {
            method: "DELETE",
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(() => loadData());
    }
}

function logout() {
    localStorage.removeItem("idToken");
    localStorage.removeItem("tokenExpiresAt");
    location.reload();
}

function handleCredentialResponse(response) {
    const idToken = response.credential;
    const payload = JSON.parse(atob(idToken.split('.')[1]));
    if (payload.email === CONFIG.ALLOWED_EMAIL) {
        token = idToken;

        localStorage.setItem("idToken", idToken);
        localStorage.setItem("tokenExpiresAt", (Date.now() + 60 * 60 * 1000).toString()); //1h

        document.getElementById("auth").classList.add("hidden");
        document.getElementById("app").classList.remove("hidden");
        loadData();
    } else {
        alert("Unauthorized email");
    }
}

function showSection(section) {
    document.getElementById("locations").classList.add("hidden");
    document.getElementById("events").classList.add("hidden");

    document.getElementById("locationForm").classList.add("hidden");
    document.getElementById("eventForm").classList.add("hidden");

    document.getElementById(section).classList.remove("hidden");
}

function loadExistingEventImages(imageUrls) {
    existingImages = imageUrls.map(url => ({
        type: 'existing',
        url,
        active: true
    }));
    images.push(...existingImages);
    console.log(images);
    renderAllImages();
}

function showLocationForm() {
    resetForms();
    document.getElementById("locationForm").classList.remove("hidden");
    setTimeout(() => {
        map.invalidateSize();
    }, 100);
}

function showEventForm() {
    resetForms();
    document.getElementById("eventForm").classList.remove("hidden");
}