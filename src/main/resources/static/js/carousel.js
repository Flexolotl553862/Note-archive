$(function () {
    let pt = 0;
    const container = document.querySelector('.card-container');
    const notes = Array.from(container.children).filter(n => n.classList.contains('card-wrapper'));
    const n = notes.length;
    const width = document.querySelector('.card-wrapper').offsetWidth;
    const height = document.querySelector('.card-wrapper').offsetHeight;
    const containerWidth = document.querySelector('.card-menu').offsetWidth - 2 * document.querySelector('.left-btn-wrapper').offsetWidth;

    const cols = Math.floor((containerWidth - 40) / width);
    const rows = Math.floor((document.querySelector('.card-menu').offsetHeight - 40) / height);
    const k = rows * cols;

    const showGroup = (start) => {
        notes.forEach(note => note.classList.remove('visible'));
        for (let i = start; i < start + k && i < n; i++) {
            notes[i].classList.add('visible');
        }
    };

    showGroup(pt);

    document.querySelector('.left-btn').addEventListener('click', function (event) {
        event.preventDefault();
        if (pt >= k) {
            pt -= k;
            showGroup(pt);
        }
    });

    document.querySelector('.right-btn').addEventListener('click', function (event) {
        event.preventDefault();
        if (pt + k < n) {
            pt += k;
            showGroup(pt);
        }
    });

    const comboBoxEl = document.querySelector('[data-hs-combo-box]');
    const input = document.getElementById('search-input');

    comboBoxEl.addEventListener('select.hs.combobox', () => {
        if (input.value !== '') {
            if (input.value !== 'All notes') {
                for (let i = 0; i < notes.length; i++) {
                    if (notes[i].id === input.value + '-frame') {
                        notes[i].classList.add('visible');
                    } else {
                        notes[i].classList.remove('visible');
                    }
                }
                document.querySelector('.left-btn').classList.add('hidden');
                document.querySelector('.right-btn').classList.add('hidden');
            } else {
                pt = 0;
                document.querySelector('.left-btn').classList.remove('hidden');
                document.querySelector('.right-btn').classList.remove('hidden');
                showGroup(pt);
            }
        }
    });
});