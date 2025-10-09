  function previewVariantImage(input, variantIndex) {
        const previewDiv = document.getElementById('image-preview-' + variantIndex);
        const img = previewDiv.querySelector('img');

        if (input.files && input.files[0]) {
            const reader = new FileReader();

            reader.onload = function(e) {
                img.src = e.target.result;
                previewDiv.style.display = 'block';
            }

            reader.readAsDataURL(input.files[0]);
        } else {
            previewDiv.style.display = 'none';
        }
    }

    function resetVariantImages() {
        const previewDivs = document.querySelectorAll('[id^="image-preview-"]');
        previewDivs.forEach(previewDiv => {
            const img = previewDiv.querySelector('img');
            if (img) {
                img.src = '';
            }
            previewDiv.style.display = 'none';
        });
    }

    function closeVariantDetailsSection() {
        const variantsSection = document.querySelector('details.variants-section');
        if (variantsSection) {
            variantsSection.removeAttribute('open');
        }
    }

    function showAddForm() {
        const form = document.getElementById('add-product-form');
        form.style.display = 'block';
    }

    function removeVariant(button) {
        const variantRows = document.querySelectorAll('.variant-row');
        if (variantRows.length > 1) {
            const row = button.closest('.variant-row');

            // Add removal animation
            row.style.animation = 'slideOut 0.3s ease-in forwards';

             // Clear inputs to ensure they don't get submitted
            const inputs = row.querySelectorAll('input');
            inputs.forEach(input => {
                input.value = '';
                // For file inputs, we need special handling
                if (input.type === 'file') {
                    input.disabled = true; // Disable so it won't be submitted
                }
            });

            decrementVariantCount();

            // Remove after animation
            setTimeout(() => {
                row.remove();
                reindexVariants();
            }, 300);
        } else {
            // Show a subtle shake animation if trying to remove the last variant
            const row = button.closest('.variant-row');
            row.style.animation = 'shake 0.5s ease-in-out';
            setTimeout(() => {
                row.style.animation = '';
            }, 500);
        }
    }

    function reindexVariants() {
        const variantRows = document.querySelectorAll('.variant-row');
        variantRows.forEach((row, index) => {
            row.setAttribute('data-variant-index', index);

            // Update all input names in this row
            const inputs = row.querySelectorAll('input');
            inputs.forEach(input => {
                const name = input.name;
                if (name && name.includes('variants[')) {
                    input.name = name.replace(/variants\[\d+\]/, `variants[${index}]`);
                    if (input.id) {
                        input.id = input.id.replace(/variants\[\d+\]/, `variants[${index}]`);
                    }
                }
            });
        });
    }
    
    
    function resetVariantsToOne() {
        const container = document.getElementById('variants-container');
        const variants = container.querySelectorAll('.variant-row');

        // Remove all except the first one
        for (let i = 1; i < variants.length; i++) {
            variants[i].remove();
        }

        // Clear the first variant's inputs
        const firstVariant = variants[0];
        const inputs = firstVariant.querySelectorAll('input');
        inputs.forEach(input => input.value = '');
        resetVariantCount();
    }
    
    function resetVariantCount() {
        const variantCount = document.getElementById("variantCount");
        variantCount.value = 1;
    }

     function decrementVariantCount() {
        const variantCount = document.getElementById("variantCount");
        variantCount.value = variantCount.value - 1;
        console.log('variantCount= '+ variantCount.value)
    }

    function handleFormSubmit(form, event) {
        console.log('handleFormSubmit');
        console.log(event);

        // Exit if target is add-variant button
        if (event.target.classList.contains('add-variant-btn')) {
            console.log('add variant button clicked');
            return;
        }

        if (event.detail.xhr.status === 200) {
            // Show success message
            const successMessage = document.getElementById('success-message');
            console.log('success message: '+ successMessage);
            successMessage.style.display = 'block';
            setTimeout(() => {
                successMessage.style.display = 'none';
            }, 8000);

            // Reset form
            form.reset();

            // Reset variant images
            resetVariantImages();

            // Reset variants
            resetVariantsToOne();

            // Close all details
            closeVariantDetailsSection();

            // Scroll to top of page
            window.scrollTo({ top: 0, behavior: 'smooth' });

        }
    }

    // HTMX event listeners
    document.body.addEventListener('htmx:beforeRequest', function(evt) {
        evt.target.classList.add('loading');
    });

    document.body.addEventListener('htmx:afterRequest', function(evt) {
        evt.target.classList.remove('loading');
    });

    // Auto-show form after successful product load
    document.body.addEventListener('htmx:afterSwap', function(evt) {
        if (evt.target.id === 'products-table-container' && evt.detail.xhr.status === 200) {
            showAddForm();
        }
    });