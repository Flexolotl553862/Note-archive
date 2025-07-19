$(function () {
    const folderId = localStorage.getItem("folderId");

    const buttons = document.querySelectorAll('.delete-button');
    const deleteButton = document.getElementById('sure-to-delete');
    const deleteModal = new Modal(document.getElementById('delete-modal'));

    const directoryForm = document.getElementById('directory-form')
    const directoryModal = new Modal(document.getElementById('directory-modal'));
    const newDirectory = document.getElementById('new-directory');

    const newFile = document.getElementById('new-file');
    const fileModal = new Modal(document.getElementById('file-modal'));
    const fileForm = document.getElementById('file-form');

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

    // delete entry section

    document.querySelectorAll('.modal-delete-hide').forEach((button) => {
        button.addEventListener('click', function () {
            deleteModal.hide();
        })
    })

    buttons.forEach(button => {
        button.addEventListener('click', function () {
            deleteModal.show();
            document.getElementById('delete-msg').textContent = 'Are you sure you want to delete ' + button.name + '?'
            localStorage.setItem('delete', button.id);
        })
    })

    deleteButton.addEventListener('submit', function (event) {
        event.preventDefault();
        const id = localStorage.getItem("delete");
        const name = document.getElementById(id).name;

        if (id !== '') {
            $.ajax({
                url: '/delete/entry',
                type: "POST",
                data: {
                    "_csrf": document.getElementsByName('_csrf')[0].value,
                    "id": id,
                    "name": name
                },
                success: function (data) {
                    deleteModal.hide();
                    window.location.reload();
                    localStorage.setItem("message", data);
                }
            })
        }
    })

    // create a directory section

    newDirectory.addEventListener('click', function () {
        directoryModal.show();
    })

    document.querySelectorAll('.modal-directory-hide').forEach((button) => {
        button.addEventListener('click', function () {
            directoryModal.hide();
        })
    })

    directoryForm.addEventListener('submit', function (event) {
        event.preventDefault()
        document.getElementById('parentId').value = folderId;
        const data = new FormData(directoryForm);
        $.ajax({
            url: "/folder/create",
            type: "POST",
            data: data,
            processData: false,
            contentType: false,
            success: function (response) {
                directoryModal.hide();
                localStorage.setItem("message", response);
                window.location.reload();
            }
        })
    })

    // create a file section

    newFile.addEventListener('click', function () {
        fileModal.show();
    })

    document.querySelectorAll('.modal-file-hide').forEach((button) => {
        button.addEventListener('click', function () {
            fileModal.hide();
        })
    })

    fileForm.addEventListener('submit', function (event) {
        event.preventDefault()
        document.getElementById('fileParentId').value = folderId;
        const data = new FormData(fileForm);

        $.ajax({
            url: "/file/create",
            type: "POST",
            data: data,
            processData: false,
            contentType: false,
            success: function (response) {
                fileModal.hide();
                localStorage.setItem("message", response);
                window.location.reload();
            }
        })
    })
})