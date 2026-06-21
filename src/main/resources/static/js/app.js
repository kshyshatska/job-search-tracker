document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-loading-form]").forEach((form) => {
        form.addEventListener("submit", () => {
            form.querySelectorAll("button[type='submit']").forEach((button) => {
                button.disabled = true;
                if (button.dataset.loadingLabel) {
                    button.dataset.originalLabel = button.textContent.trim();
                    button.textContent = button.dataset.loadingLabel;
                }
            });
        });
    });

    document.querySelectorAll("[data-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll(".alert").forEach((alert) => {
        window.setTimeout(() => alert.classList.add("fade-out"), 4200);
    });
});
