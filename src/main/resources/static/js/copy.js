window.addEventListener('load', function () {
    document.querySelectorAll('.copy-button').forEach((button) => {
        button.addEventListener('click', function () {
            event.preventDefault()
            button.disabled = true
            navigator.clipboard.writeText(button.children[0].value).then(r => {
                show(button)
            });
            setTimeout(function () {
                reset(button);
                button.disabled = false;
            }, 2000);
        })
    })

    const show = (button) => {
        button.children[1].classList.add('hidden');
        button.children[2].classList.remove('hidden');
    }

    const reset = (button) => {
        button.children[2].classList.add('hidden');
        button.children[1].classList.remove('hidden');
    }
})