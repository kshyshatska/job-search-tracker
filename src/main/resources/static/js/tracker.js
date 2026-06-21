document.addEventListener("DOMContentLoaded", () => {
    const board = document.querySelector("[data-kanban-board]");
    if (!board) {
        return;
    }

    const csrfHeader = board.dataset.csrfHeader;
    const csrfToken = board.dataset.csrfToken;

    const showToast = (message, type = "success") => {
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        window.setTimeout(() => toast.classList.add("show"), 20);
        window.setTimeout(() => {
            toast.classList.remove("show");
            window.setTimeout(() => toast.remove(), 250);
        }, 2400);
    };

    const patchStatus = async (applicationId, status) => {
        const response = await fetch(`/api/applications/${applicationId}/status`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ status })
        });
        if (!response.ok) {
            throw new Error("Status update failed");
        }
        return response.json();
    };

    board.querySelectorAll(".kanban-card").forEach((card) => {
        card.setAttribute("draggable", "true");
        card.addEventListener("dragstart", () => card.classList.add("dragging"));
        card.addEventListener("dragend", () => card.classList.remove("dragging"));
    });

    board.querySelectorAll("[data-status-column]").forEach((column) => {
        column.addEventListener("dragover", (event) => {
            event.preventDefault();
            column.classList.add("drop-target");
        });
        column.addEventListener("dragleave", () => column.classList.remove("drop-target"));
        column.addEventListener("drop", async (event) => {
            event.preventDefault();
            column.classList.remove("drop-target");
            const card = board.querySelector(".kanban-card.dragging");
            if (!card || card.closest("[data-status-column]") === column) {
                return;
            }

            const previousColumn = card.closest("[data-status-column]");
            const status = column.dataset.statusColumn;
            column.querySelector(".kanban-items").prepend(card);
            card.querySelector(".compact-select").value = status;

            try {
                await patchStatus(card.dataset.applicationId, status);
                showToast("Статус оновлено");
                window.setTimeout(() => window.location.reload(), 500);
            } catch (error) {
                previousColumn.querySelector(".kanban-items").prepend(card);
                showToast("Не вдалося оновити статус", "error");
            }
        });
    });

    board.querySelectorAll(".compact-select").forEach((select) => {
        select.addEventListener("change", async () => {
            const card = select.closest(".kanban-card");
            try {
                await patchStatus(card.dataset.applicationId, select.value);
                showToast("Статус оновлено");
                window.setTimeout(() => window.location.reload(), 500);
            } catch (error) {
                showToast("Не вдалося оновити статус", "error");
            }
        });
    });
});
