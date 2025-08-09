let left = 0, right = 0;

const container = document.querySelector('.card-container');
const cardMenu = document.querySelector('.card-menu');
const leftBtn = document.querySelector('.left-btn');
const rightBtn = document.querySelector('.right-btn');
const notes = Array.from(container.children).filter(n => n.classList.contains('card-wrapper'));
const n = notes.length;

let width, height, cols, rows, k, lock = false;

function recalculateLayout() {
    if (n === 0) return;
    width = document.querySelector('.card-wrapper').offsetWidth;
    height = document.querySelector('.card-wrapper').offsetHeight;

    const containerWidth = cardMenu.offsetWidth - 2 * document.querySelector('.left-btn-wrapper').offsetWidth;
    const containerHeight = cardMenu.offsetHeight;

    cols = Math.max(1, Math.floor((containerWidth - 60) / width));
    rows = Math.max(1, Math.floor((containerHeight - 40) / height));
    k = rows * cols;
}

function showGroup() {
    notes.forEach(note => note.classList.remove('visible'));
    for (let i = left; i <= right; i++) {
        notes[i].classList.add('visible');
    }
    let input = document.getElementById('search-input').value;
    let hideButtons = input !== null && input !== '' && input !== 'All notes';
    if (right === n - 1 || hideButtons) {
        rightBtn.style.visibility = 'hidden';
    } else {
        rightBtn.style.visibility = 'visible';
    }
    if (left === 0 || hideButtons) {
        leftBtn.style.visibility = 'hidden';
    } else {
        leftBtn.style.visibility = 'visible';
    }
}

function refreshCarousel() {
    recalculateLayout();
    showGroup();
}

window.addEventListener('load', function () {
    recalculateLayout();
    left = 0;
    right = Math.min(n - 1, k - 1);
    showGroup();
});

window.addEventListener('resize', function () {
    refreshCarousel();
});

$(function () {
    leftBtn.addEventListener('click', function (event) {
        event.preventDefault();
        right = left - 1;
        left = Math.max(0, right - k + 1);
        showGroup()
    });

    rightBtn.addEventListener('click', function (event) {
        event.preventDefault();
        left = right + 1;
        right = Math.min(n - 1, left + k - 1);
        showGroup()
    });

    const comboBoxEl = document.querySelector('[data-hs-combo-box]');
    const input = document.getElementById('search-input');

    if (comboBoxEl) {
        comboBoxEl.addEventListener('select.hs.combobox', () => {
            if (input.value !== '') {
                if (input.value !== 'All notes') {
                    for (let i = 0; i < notes.length; i++) {
                        if (notes[i].id === input.value + '-frame') {
                            left = i;
                            right = i;
                            showGroup()
                        }
                    }
                } else {
                    left = 0;
                    right = Math.min(n - 1, k - 1);
                    refreshCarousel();
                }
            }
        });
    }
});
