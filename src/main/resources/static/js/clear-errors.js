let refresh = function () {
    const inputs = document.querySelectorAll('.my-input');
    const saveInputs = document.querySelectorAll('.save-input');
    const fileInputs = document.querySelectorAll('.my-file-input');
    const confirmation = document.getElementById('password-confirmation');
    const password = document.getElementById('password');
    const form = document.getElementById('register-form')

    if (inputs != null) {
        inputs.forEach(input => {
            const error = input.nextElementSibling.nextElementSibling;

            input.addEventListener('input', function () {
                if (input.value.trim() !== '') {
                    error.classList.add('hidden');
                    input.classList.remove('bg-red-100', 'border', 'border-red-500');
                    input.classList.replace('text-red-900', 'text-gray-900')
                }
            });
        });
    }

    if (saveInputs != null) {
        saveInputs.forEach(input => {
            input.value = localStorage.getItem(input.id) || "";

            input.addEventListener('input', function () {
                localStorage.setItem(event.target.name, event.target.value);
            });
        });
    }

    if (fileInputs != null) {
        fileInputs.forEach(input => {
            const error = input.nextElementSibling;

            input.addEventListener('input', function () {
                error.classList.add('hidden');
            });
        });
    }

    if (form != null && confirmation != null && password != null) {
        form.addEventListener('submit', function (event) {
            if (confirmation.value !== password.value) {
                event.preventDefault()
                const error = confirmation.nextElementSibling.nextElementSibling
                error.classList.remove('hidden')
                error.textContent = 'passwords doesnt match'
                confirmation.classList.add('bg-red-100', 'border', 'border-red-500');
                confirmation.classList.replace('text-gray-900', 'text-red-900');
            }
        });
    }
}

document.addEventListener("DOMContentLoaded", function () {
    refresh();
});