  function previewVariantImage(input, variantIndex) {
        console.log('previewVariantImage');
        const previewDiv = input.nextElementSibling;
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

    function closeVariantDetailsSection() {
        console.log('closeVariantDetailsSection');
        const variantsSection = document.querySelector('details.variants-section');
        if (variantsSection) {
            variantsSection.removeAttribute('open');
        }
    }

    function showAddForm() {
        const form = document.getElementById('add-product-form');
        form.style.display = 'block';
    }


   function addVariant() {
       const variantCount = document.getElementById("variantCount");
       const currentCount = parseInt(variantCount.value);

       if (currentCount < 3) {
           // Find first invisible variant
           for (let i = 0; i < 3; i++) {
               const checkbox = document.getElementById(`variantVisible${i}`);
               if (checkbox && !checkbox.checked) {
                   // Make this variant visible
                   checkbox.checked = true;
                   checkbox.setAttribute('checked', 'checked');

                   // Show the row
                   const variantDiv = document.getElementById('variant_' + i);
                   if (variantDiv) {
                       variantDiv.style.display = 'block';

                       // Enable inputs
                       const inputs = variantDiv.querySelectorAll('input, select');
                       inputs.forEach(input => input.disabled = false);

                       // Increment count
                       variantCount.value = currentCount + 1;

                       // Update button state
                       if (currentCount + 1 >= 3) {
                           document.querySelector('.add-variant-btn').disabled = true;
                       }

                       break; // Found one, exit loop
                   }
               }
           }
       }
   }

   function removeVariant(button) {
       const variantCount = document.getElementById("variantCount");
       const currentCount = parseInt(variantCount.value);

       if (currentCount > 0) { // Keep at least one
           const row = button.closest('.variant-row');
           const rowIndex = row.getAttribute('data-variant-index');
           console.log('rowIndex:', rowIndex, 'row:', row);
           const variantDiv = document.getElementById('variant_' + rowIndex);
           if (!variantDiv) {
                return;
           }

           // Mark as invisible
           const checkbox = document.getElementById('variantVisible' + rowIndex);
           if (checkbox) {
               checkbox.checked = false;
               checkbox.removeAttribute('checked'); // Remove the HTML attribute
           }

           // Add animation
           variantDiv.style.animation = 'slideOut 0.3s ease-in forwards';

           // Clear and disable inputs
           const inputs = row.querySelectorAll('input, select');
           inputs.forEach(input => {
               input.value = '';
               input.disabled = true;
           });

           // Clear preview image
           const previewDiv = document.getElementById('variant_' + rowIndex + 'imagePreview');
           if (previewDiv) {
               const img = previewDiv.querySelector('img');
               if (img) {
                   img.src = '';
               }
               previewDiv.style.display = 'none';
           }

           // Hide after animation
           setTimeout(() => {
               variantDiv.style.display = 'none';

               // Enable add button if needed
               document.querySelector('.add-variant-btn').disabled = false;

               // Decrement count
               variantCount.value = currentCount - 1;
           }, 300);
       } else {
           // Show shake animation
           const row = button.closest('.variant-row');
           row.style.animation = 'shake 0.5s ease-in-out';
           setTimeout(() => {
               row.style.animation = '';
           }, 500);
       }
   }

function initializeVariantRows() {
    console.log('initializeVariantRows');
    // Count visible variants
    let visibleCount = 0;
    for (let i = 0; i < 3; i++) {
        const checkbox = document.getElementById(`variantVisible${i}`);
        if (checkbox && checkbox.checked) {
            visibleCount++;

            // Show the variant
            const variantDiv = document.getElementById('variant_' + i);
            if (variantDiv) {
                variantDiv.style.display = 'block';

                // Enable inputs
                const inputs = variantDiv.querySelectorAll('input, select');
                inputs.forEach(input => input.disabled = false);
            }
        } else {
            // Hide the variant
            const variantDiv = document.getElementById('variant_' + i);
            if (variantDiv) {
                variantDiv.style.display = 'none';

                // Disable inputs
                const inputs = variantDiv.querySelectorAll('input, select');
                inputs.forEach(input => input.disabled = true);
            }
        }
    }

    // Update count and button state
    const variantCount = document.getElementById("variantCount");
    if (variantCount) {
        variantCount.value = visibleCount;
    }

    const addButton = document.querySelector('.add-variant-btn');
    if (addButton) {
        addButton.disabled = visibleCount >= 3;
    }
}

// Run on page load and after HTMX swaps
document.addEventListener('DOMContentLoaded', initializeVariantRows);
document.body.addEventListener('htmx:afterSwap', function(evt) {
    if (evt.target.id === 'product-form' || evt.target.closest('#product-form')) {
        initializeVariantRows();
    }
});

function handleSaveSuccess(dialog, event) {
    console.log('handleSaveSuccess');
    console.log(dialog);
    console.log(event);
    htmx.trigger('body', 'refresh-products');
    document.getElementById('products-table-container').scrollIntoView({behavior: 'smooth'})
}

function handleFormSubmit(form, event) {
    console.log('handleFormSubmit');
    console.log(event);

    // Exit if target is add-variant button
    if (event.target.classList.contains('add-variant-btn')) {
        console.log('add variant button clicked');
        return;
    }
    console.log(event.detail.xhr.status)
    htmx.trigger('body', 'refresh-products');

    // Show success message
    //const successMessage = document.getElementById('success-container');
    //console.log('success message: '+ successMessage);
    //successMessage.style.display = 'block';

    // Show success dialog
    const dialog = document.querySelector('.dialog-light-dismiss');
    dialog.open = true;
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
//    if (evt.target.id === 'product-form' && evt.detail.xhr.status === 200) {
//        console.log('>>> add product form submitted');
//        handleFormSubmit(evt.target, evt);
//    }
    if (evt.target.id === 'product-form' && evt.detail.xhr.status !== 200) {
        console.log('>>> add product form submitted');
        const firstError = document.querySelector('.error-message');
        if (firstError) {
            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } else {
            const firstErrorInput = document.querySelector('div.error');
            if (firstErrorInput) {
                firstErrorInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    }
});
