const results = document.getElementById("results");
const statusEl = document.getElementById("status");
const detailEl = document.getElementById("detail");
const pager = document.getElementById("pager");
const pageLabel = document.getElementById("page-label");
const prevBtn = document.getElementById("prev");
const nextBtn = document.getElementById("next");

const PAGE_SIZE = 12;
let lastList = null;

document.querySelectorAll(".tabs [role='tab']").forEach((tab) => {
  tab.addEventListener("click", () => selectTab(tab.id.replace("tab-", "")));
});

document.querySelectorAll("[data-point]").forEach((select) => {
  select.addEventListener("change", () => applyPreset(select));
});

document.querySelectorAll(".chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    document.getElementById("q").value = chip.textContent.trim();
    if (chip.dataset.needsPoint === "true") {
      const preset = document.getElementById("search-preset");
      preset.value = "28.4595,77.0266";
      applyPreset(preset);
    }
  });
});

document.getElementById("search-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const params = new URLSearchParams();
  params.set("q", value("q"));
  const point = pointOf("search");
  if (point) {
    params.set("lat", point.lat);
    params.set("lng", point.lng);
  }
  loadList("/v1/restaurants/search", params, 1);
});

document.getElementById("browse-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const params = new URLSearchParams();
  ["city", "cuisines", "name", "minRating", "maxCostForTwo", "maxDistanceKm", "sort"].forEach((name) => {
    const v = value(name);
    if (v) params.set(name, v);
  });
  const point = pointOf("browse");
  if (point) {
    params.set("lat", point.lat);
    params.set("lng", point.lng);
  }
  if ((value("sort") === "distance" || value("maxDistanceKm")) && !point) {
    showStatus("Nearest and “within km” need a starting point. Pick Gurgaon center or enter coordinates.");
    return;
  }
  loadList("/v1/restaurants", params, 1);
});

document.getElementById("add-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const body = {
    name: value("add-name"),
    city: value("add-city"),
  };
  setIf(body, "address", value("add-address"));
  setIf(body, "locality", value("add-locality"));
  setIf(body, "currency", value("add-currency"));
  const cuisines = value("add-cuisines");
  if (cuisines) {
    body.cuisines = cuisines.split(",").map((s) => s.trim()).filter(Boolean);
  }
  const cost = value("add-cost");
  if (cost) body.avgCostForTwo = Number(cost);
  const lat = value("add-lat");
  const lng = value("add-lng");
  if (lat) body.latitude = Number(lat);
  if (lng) body.longitude = Number(lng);
  body.tableBooking = document.getElementById("add-booking").checked;
  body.onlineDelivery = document.getElementById("add-delivery").checked;
  body.deliveringNow = document.getElementById("add-now").checked;

  clearOutput();
  showStatus("Saving…");
  try {
    const res = await fetch("/v1/restaurants", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const payload = await res.json();
    if (!res.ok) {
      showStatus(errorText(payload, res.status));
      return;
    }
    showStatus(`Added “${payload.name}” with id ${payload.id}.`);
    renderDetail(payload);
    event.target.reset();
    document.getElementById("add-currency").value = "Rs.";
  } catch {
    showStatus("Request failed. Is the API up?");
  }
});

document.getElementById("lookup-form").addEventListener("submit", (event) => {
  event.preventDefault();
  openDetail(value("lookup-id"));
});

prevBtn.addEventListener("click", () => {
  if (lastList && lastList.page > 1) loadList(lastList.url, lastList.params, lastList.page - 1);
});

nextBtn.addEventListener("click", () => {
  if (lastList && lastList.page * PAGE_SIZE < lastList.total) {
    loadList(lastList.url, lastList.params, lastList.page + 1);
  }
});

function selectTab(name) {
  document.querySelectorAll(".tabs [role='tab']").forEach((tab) => {
    const on = tab.id === `tab-${name}`;
    tab.setAttribute("aria-selected", on ? "true" : "false");
  });
  ["search", "browse", "add", "lookup"].forEach((id) => {
    document.getElementById(`panel-${id}`).hidden = id !== name;
  });
}

function applyPreset(select) {
  const scope = select.dataset.point;
  const coords = document.querySelectorAll(`[data-coords="${scope}"]`);
  const lat = document.getElementById(`${scope}-lat`);
  const lng = document.getElementById(`${scope}-lng`);
  const custom = select.value === "custom";
  const preset = select.value.includes(",");
  coords.forEach((el) => {
    el.hidden = !custom && !preset;
  });
  if (preset) {
    const [la, ln] = select.value.split(",");
    lat.value = la;
    lng.value = ln;
    lat.readOnly = true;
    lng.readOnly = true;
  } else {
    lat.readOnly = false;
    lng.readOnly = false;
    if (!custom) {
      lat.value = "";
      lng.value = "";
    }
  }
}

function pointOf(scope) {
  const lat = value(`${scope}-lat`);
  const lng = value(`${scope}-lng`);
  if (!lat && !lng) return null;
  return { lat, lng };
}

async function loadList(url, params, page) {
  const query = new URLSearchParams(params);
  query.set("page", String(page));
  query.set("size", String(PAGE_SIZE));
  clearOutput();
  showStatus("Loading…");
  try {
    const res = await fetch(`${url}?${query.toString()}`);
    const body = await res.json();
    if (!res.ok) {
      showStatus(errorText(body, res.status));
      return;
    }
    const items = body.items || [];
    lastList = { url, params, page: body.page, total: body.total };
    const from = items.length === 0 ? 0 : (body.page - 1) * body.size + 1;
    const to = (body.page - 1) * body.size + items.length;
    showStatus(items.length === 0 ? "Nothing matched." : `${body.total} restaurant(s). Showing ${from}–${to}.`);
    results.innerHTML = items.map(card).join("");
    results.querySelectorAll(".card").forEach((el) => {
      el.addEventListener("click", () => openDetail(el.dataset.id));
    });
    const more = body.page * body.size < body.total;
    pager.hidden = body.total <= body.size && body.page === 1;
    prevBtn.disabled = body.page <= 1;
    nextBtn.disabled = !more;
    pageLabel.textContent = `Page ${body.page}`;
  } catch {
    showStatus("Request failed. Is the API up?");
  }
}

async function openDetail(id) {
  if (!id) return;
  detailEl.hidden = true;
  detailEl.innerHTML = "";
  showStatus("Loading restaurant…");
  try {
    const res = await fetch(`/v1/restaurants/${encodeURIComponent(id)}`);
    const body = await res.json();
    if (!res.ok) {
      showStatus(errorText(body, res.status));
      return;
    }
    showStatus(`Restaurant ${body.id}`);
    renderDetail(body);
    detailEl.scrollIntoView({ behavior: "smooth", block: "nearest" });
  } catch {
    showStatus("Request failed. Is the API up?");
  }
}

function renderDetail(r) {
  const rows = [
    ["Id", r.id],
    ["City", r.city],
    ["Locality", r.locality],
    ["Address", r.address],
    ["Country", r.country],
    ["Cuisines", (r.cuisines || []).join(", ")],
    ["Rating", r.averageRating == null ? "" : `${Number(r.averageRating).toFixed(1)} (${r.votes ?? 0} votes)`],
    ["Cost for two", r.avgCostForTwo == null ? "" : `${r.currency || ""} ${r.avgCostForTwo}`.trim()],
    ["Table booking", yesNo(r.tableBooking)],
    ["Online delivery", yesNo(r.onlineDelivery)],
    ["Delivering now", yesNo(r.deliveringNow)],
    ["Coordinates", r.latitude == null ? "" : `${r.latitude}, ${r.longitude}`],
  ].filter(([, v]) => v !== "" && v != null);
  detailEl.hidden = false;
  detailEl.innerHTML = `<article class="detail">
    <h2>${escapeHtml(r.name)}</h2>
    <dl>
      ${rows
        .map(([k, v]) => `<dt>${escapeHtml(k)}</dt><dd>${escapeHtml(v)}</dd>`)
        .join("")}
    </dl>
  </article>`;
}

function card(r) {
  const cuisines = (r.cuisines || []).join(", ");
  const rating = r.averageRating == null ? "unrated" : Number(r.averageRating).toFixed(1);
  const cost =
    r.avgCostForTwo == null
      ? ""
      : `<div class="meta">${escapeHtml(r.currency || "Rs.")} ${r.avgCostForTwo} for two</div>`;
  const distance = r.distanceKm == null ? "" : `<div class="meta">${r.distanceKm} km from the starting point</div>`;
  return `<button type="button" class="card" data-id="${escapeHtml(r.id)}">
    <h2>${escapeHtml(r.name)} <span class="rating">${rating}</span></h2>
    <div class="meta">${escapeHtml([r.city, cuisines].filter(Boolean).join(" · "))}</div>
    <div class="meta">${escapeHtml(r.address || "")}</div>
    ${cost}
    ${distance}
  </button>`;
}

function yesNo(value) {
  if (value == null) return "";
  return value ? "Yes" : "No";
}

function setIf(body, key, val) {
  if (val) body[key] = val;
}

function value(id) {
  return document.getElementById(id).value.trim();
}

function clearOutput() {
  results.innerHTML = "";
  pager.hidden = true;
  detailEl.hidden = true;
  detailEl.innerHTML = "";
  lastList = null;
}

function showStatus(text) {
  statusEl.textContent = text;
}

function errorText(body, status) {
  const details = (body.details || []).filter(Boolean).join(" ");
  return [body.message || `HTTP ${status}`, details].filter(Boolean).join(" ");
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
