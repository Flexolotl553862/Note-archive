$(function () {
    const getSuccessElement = function (message) {
        return '<div class="flex success">\n' +
            '    <div class="shrink-0">\n' +
            '        <span class="inline-flex justify-center items-center size-8 rounded-full border-4 border-teal-100 bg-teal-200 text-teal-800 dark:border-teal-900 dark:bg-teal-800 dark:text-teal-400">\n' +
            '          <svg class="shrink-0 size-4" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"\n' +
            '               fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">\n' +
            '            <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"></path>\n' +
            '            <path d="m9 12 2 2 4-4"></path>\n' +
            '          </svg>\n' +
            '        </span>\n' +
            '    </div>\n' +
            '    <div class="ms-3">\n' +
            '        <h3 id="hs-bordered-success-style-label" class="text-gray-800 font-semibold dark:text-white"\n' +
            '            >' + message.title + '</h3>\n' +
            '        <p class="text-sm text-gray-700 dark:text-neutral-400">' + message.text + '</p>\n' +
            '    </div>\n' +
            '</div>'
    }

    const getErrorElement = function (message) {
        return '<div class="flex error">\n' +
            '    <div class="shrink-0">\n' +
            '        <span class="inline-flex justify-center items-center size-8 rounded-full border-4 border-red-100 bg-red-200 text-red-800 dark:border-red-900 dark:bg-red-800 dark:text-red-400">\n' +
            '          <svg class="shrink-0 size-4" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"\n' +
            '               fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">\n' +
            '            <path d="M18 6 6 18"></path>\n' +
            '            <path d="m6 6 12 12"></path>\n' +
            '          </svg>\n' +
            '        </span>\n' +
            '    </div>\n' +
            '    <div class="ms-3">\n' +
            '        <h3 id="hs-bordered-red-style-label" class="text-gray-800 font-semibold dark:text-white">' + message.title + '</h3>\n' +
            '        <p class="text-sm text-gray-700 dark:text-neutral-400">' + message.text + '</p>\n' +
            '    </div>\n' +
            '</div>';
    }

    const notify = function () {
        if (localStorage.getItem('message') && localStorage.getItem('message') !== '') {
            const message = JSON.parse(localStorage.getItem('message'));
            localStorage.removeItem('message');
            const ok = message.ok;
            const notification = ok ? getSuccessElement(message) : getErrorElement(message);
            Toastify({
                text: notification,
                duration: 2000,
                position: "center",
                gravity: "bottom",
                style: {
                    background: ok ? "#f0fdfa" : "#fef2f2"
                },
                className: ok ? "bg-teal-50 border-t-2 border-teal-500 rounded-lg p-4 dark:bg-teal-800/30" : "border-s-4 border-red-500 p-4 dark:bg-red-800/30",
                close: false,
                escapeMarkup: false
            }).showToast();
        }
    }

    notify();

    const modals = new Map();
    const modalData = new Map();

    document.querySelectorAll('.modal').forEach((modal) => {
        modals.set(modal.id, new Modal(modal));
    })

    document.querySelectorAll('.modal-hide').forEach((button) => {
        button.addEventListener('click', function () {
            modals.get(button.dataset.modal).hide();
        })
    })

    document.querySelectorAll('.modal-show').forEach((button) => {
        button.addEventListener('click', function () {
            if (document.getElementById(button.dataset.modal + '-msg')) {
                document.getElementById(button.dataset.modal + '-msg').textContent = button.dataset.message;
            }
            modalData.set(button.dataset.modal, button.dataset);
            modals.get(button.dataset.modal).show();
        })
    })

    document.querySelectorAll('.modal-form').forEach((form) => {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            data = new FormData(form);
            let url = modalData.get(form.dataset.modal).href;
            if (url === '' || url === undefined) {
                url = form.getAttribute('action')
            }
            for (let [name, value] of data.entries()) {
                if (value === '' && modalData.has(form.dataset.modal)) {
                    data.set(name, modalData.get(form.dataset.modal)[name.toLowerCase()]);
                }
            }
            $.ajax({
                url: url,
                type: "POST",
                data: data,
                processData: false,
                contentType: false,
                success: function (data) {
                    modals.get(form.dataset.modal).hide();
                    localStorage.setItem("message", JSON.stringify(data));
                    window.location.reload();
                },
                error: function () {
                    modals.get(form.dataset.modal).hide();
                    localStorage.setItem("message", JSON.stringify({
                        ok: false,
                        title: "Error!",
                        text: "Something went wrong."
                    }));
                    // window.location.reload();
                }
            })
        })
    })
})