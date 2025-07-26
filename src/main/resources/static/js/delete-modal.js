$(function () {
    const buttons = document.querySelectorAll('.delete-button');
    const deleteButton = document.getElementById('sure-to-delete');
    const deleteModal = new Modal(document.getElementById('delete-modal'));

    const notify = function () {
        const message = localStorage.getItem('message');
        if (message) {
            localStorage.removeItem('message');
            const response = document.createRange().createContextualFragment(message);
            const ok = response.querySelectorAll('.success').length > 0;

            Toastify({
                text: message,
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

    document.querySelectorAll('.modal-delete-hide').forEach((button) => {
        button.addEventListener('click', function () {
            deleteModal.hide();
        })
    })

    buttons.forEach(button => {
        button.addEventListener('click', function () {
            deleteModal.show();
            // document.getElementById('delete-msg').textContent = 'Are you sure you want to delete ' + button.name + '?'
            localStorage.setItem('delete', button.id);
        })
    })

    deleteButton.addEventListener('submit', function (event) {
        event.preventDefault();
        const id = localStorage.getItem("delete");

        if (id !== '') {
            $.ajax({
                url: '/delete/note',
                type: "POST",
                data: {
                    "_csrf": document.getElementsByName('_csrf')[0].value,
                    "id": id,
                },
                success: function (data) {
                    deleteModal.hide();
                    window.location.reload();
                    localStorage.setItem("message", data);
                }
            })
        }
    })
})