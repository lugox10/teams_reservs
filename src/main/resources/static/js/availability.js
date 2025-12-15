// availability.js
(() => {
  // helpers
  const qs = sel => document.querySelector(sel);
  const qsa = sel => Array.from(document.querySelectorAll(sel));

  let currentFieldId = null;
  let minBooking = 1;
  let slotMinutes = 60;
  let selectedHours = []; // array de ints
  let currentPricePerHour = null;

  // open modal when click "Ver disponibilidad"
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.btn-availability');
    if (!btn) return;
    currentFieldId = btn.dataset.fieldId;
    minBooking = parseInt(btn.dataset.min || '1', 10);
    currentPricePerHour = parseFloat(btn.dataset.price || '0');
    slotMinutes = parseInt(btn.dataset.slotMinutes || '60', 10); // safe fallback
    showModal();
  });

  function showModal() {
    const modal = qs('#availabilityModal');
    modal.style.display = 'block';
    modal.setAttribute('aria-hidden', 'false');
    const today = new Date().toISOString().slice(0,10);
    qs('#availabilityDate').value = today;
    loadSlotsForDate(today);
  }

  // close
  qs('#modalClose').addEventListener('click', closeModal);
  function closeModal() {
    const modal = qs('#availabilityModal');
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
    selectedHours = [];
    updateSummary();
  }

  // load slots
  qs('#loadSlots').addEventListener('click', () => {
    const d = qs('#availabilityDate').value;
    if (!d) return alert('Selecciona fecha');
    loadSlotsForDate(d);
  });

  async function loadSlotsForDate(date) {
    selectedHours = [];
    updateSummary();
    qs('#slotsContainer').innerHTML = 'Cargando...';
    try {
      const res = await fetch(`/api/fields/${currentFieldId}/availability?date=${date}`);
      if (!res.ok) throw new Error('Error cargando disponibilidad');
      const data = await res.json();
      renderSlotsGrid(data.slots || []);
    } catch (err) {
      console.error(err);
      qs('#slotsContainer').innerHTML = '<p>Error al cargar</p>';
    }
  }

  function renderSlotsGrid(slots) {
    const container = qs('#slotsContainer');
    container.innerHTML = '';
    // slots expected to include startDateTime or hour label
    slots.forEach(slot => {
      // compute hour number from startDateTime if present
      let hour = null;
      if (slot.startDateTime) {
        hour = new Date(slot.startDateTime).getHours();
      } else if (slot.slotLabel) {
        const m = slot.slotLabel.match(/^(\d{2}):/);
        hour = m ? parseInt(m[1], 10) : null;
      } else if (slot.hour !== undefined) {
        hour = slot.hour;
      }

      const btn = document.createElement('button');
      btn.className = 'slot-btn';
      btn.dataset.hour = hour;
      btn.type = 'button';
      btn.textContent = slot.slotLabel ? slot.slotLabel : `${hour}:00`;
      if (slot.status === 'BOOKED' || slot.status === 'RESERVADO') {
        btn.disabled = true;
        btn.classList.add('booked');
      } else if (slot.status === 'BLOCKED') {
        btn.disabled = true;
        btn.classList.add('blocked');
      } else {
        btn.classList.add('available');
        btn.addEventListener('click', onSlotClick);
      }
      container.appendChild(btn);
    });
  }

  // selection logic: must be contiguous
  function onSlotClick(e) {
    const hour = parseInt(e.currentTarget.dataset.hour, 10);
    if (!Number.isFinite(hour)) return;
    if (selectedHours.includes(hour)) {
      selectedHours = selectedHours.filter(h => h !== hour);
    } else {
      // add but ensure contiguous with existing selection
      if (selectedHours.length === 0) {
        selectedHours.push(hour);
      } else {
        const min = Math.min(...selectedHours);
        const max = Math.max(...selectedHours);
        if (hour === min - 1 || hour === max + 1) {
          selectedHours.push(hour);
        } else {
          // not contiguous: reset selection to the clicked hour
          selectedHours = [hour];
        }
      }
    }
    selectedHours.sort((a,b)=>a-b);
    updateUISelection();
  }

  function updateUISelection() {
    qsa('.slot-btn').forEach(b => {
      const h = parseInt(b.dataset.hour, 10);
      if (selectedHours.includes(h)) {
        b.classList.add('selected');
      } else {
        b.classList.remove('selected');
      }
    });
    updateSummary();
  }

  function updateSummary() {
    const summary = qs('#selectionSummary');
    if (selectedHours.length === 0) {
      summary.style.display = 'none';
      return;
    }
    const hoursCount = selectedHours.length;
    // check minBooking
    if (hoursCount < minBooking) {
      qs('#selLabel').textContent = `${hoursCount}h (mínimo ${minBooking}h)`;
    } else {
      qs('#selLabel').textContent = `${selectedHours[0]}:00 - ${selectedHours[selectedHours.length-1]+1}:00 (${hoursCount}h)`;
    }
    const total = (currentPricePerHour || 0) * hoursCount;
    qs('#selPrice').textContent = total.toFixed(2);
    summary.style.display = 'block';
  }

  // confirm reservation (sends to REST endpoint)
  qs('#confirmReservation').addEventListener('click', async () => {
    if (selectedHours.length === 0) return alert('Selecciona al menos 1 hora');
    const hoursCount = selectedHours.length;
    if (hoursCount < minBooking) return alert(`Debes reservar al menos ${minBooking} horas`);
    // build request
    const startHour = selectedHours[0];
    const duration = hoursCount;
    const date = qs('#availabilityDate').value;
    const paymentMethod = qs('#paymentMethod').value;
    const payload = {
      fieldId: parseInt(currentFieldId,10),
      // assuming your API expects start as ISO datetime; adapt si tu Request DTO usa startDateTime
      date: date,
      startHour: startHour,
      durationHours: duration,
      paymentMethod: paymentMethod
    };
    try {
      const res = await fetch('/api/reservations', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(payload)
      });
      if (res.status === 201 || res.ok) {
        alert('Reserva creada correctamente');
        closeModal();
      } else {
        const err = await res.text();
        alert('Error al reservar: ' + err);
      }
    } catch (err) {
      console.error(err);
      alert('Error al crear reserva');
    }
  });

})();
