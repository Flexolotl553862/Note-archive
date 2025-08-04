let pt = 0;

const container = document.querySelector('.card-container');
const cardMenu = document.querySelector('.card-menu');
const leftBtn = document.querySelector('.left-btn');
const rightBtn = document.querySelector('.right-btn');
const notes = Array.from(container.children).filter(n => n.classList.contains('card-wrapper'));
const n = notes.length;

let width, height, cols, rows, k;

function recalculateLayout() {
    if (n === 0) return;
    width = document.querySelector('.card-wrapper').offsetWidth;
    height = document.querySelector('.card-wrapper').offsetHeight;

    const containerWidth = cardMenu.offsetWidth - 2 * document.querySelector('.left-btn-wrapper').offsetWidth;
    const containerHeight = cardMenu.offsetHeight;

    cols = Math.max(1, Math.floor((containerWidth - 60) / width));
    rows = Math.max(1, Math.floor((containerHeight - 40) / height));
    k = rows * cols;

    if (pt >= n) pt = 0; // prevent overflow
}

function showGroup(start) {
    notes.forEach(note => note.classList.remove('visible'));
    for (let i = start; i < start + k && i < n; i++) {
        notes[i].classList.add('visible');
    }
    if (pt + k >= n) {
        rightBtn.style.visibility = 'hidden';
    } else {
        rightBtn.style.visibility = 'visible';
    }
    if (pt - k < 0) {
        leftBtn.style.visibility = 'hidden';
    } else {
        leftBtn.style.visibility = 'visible';
    }
}

function refreshCarousel() {
    recalculateLayout();
    showGroup(pt);
}

window.addEventListener('load', function () {
    refreshCarousel();
});

window.addEventListener('resize', function () {
    notes.forEach(note => note.classList.remove('visible'));
    refreshCarousel();
});

$(function () {
    leftBtn.addEventListener('click', function (event) {
        event.preventDefault();
        if (pt >= k) {
            pt -= k;
            showGroup(pt);
        }
    });

    rightBtn.addEventListener('click', function (event) {
        event.preventDefault();
        if (pt + k < n) {
            pt += k;
            showGroup(pt);
        }
    });

    const comboBoxEl = document.querySelector('[data-hs-combo-box]');
    const input = document.getElementById('search-input');

    if (comboBoxEl) {
        comboBoxEl.addEventListener('select.hs.combobox', () => {
            if (input.value !== '') {
                if (input.value !== 'All notes') {
                    for (let i = 0; i < notes.length; i++) {
                        notes[i].classList.toggle('visible', notes[i].id === input.value + '-frame');
                    }
                    leftBtn.classList.add('hidden');
                    rightBtn.classList.add('hidden');
                } else {
                    pt = 0;
                    leftBtn.classList.remove('hidden');
                    rightBtn.classList.remove('hidden');
                    refreshCarousel();
                }
            }
        });
    }
});
