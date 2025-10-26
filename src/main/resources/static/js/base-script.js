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

       if (currentCount > 0) {
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
document.addEventListener('DOMContentLoaded', function() {
        initializeVariantRows();
});
document.body.addEventListener('htmx:afterSwap', function(evt) {
    if (evt.target.id === 'product-form' || evt.target.closest('#product-form')
        || evt.target.id === 'forms-container' || evt.target.closest('#forms-container')) {
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


// HTMX event listeners
document.body.addEventListener('htmx:beforeRequest', function(evt) {
    evt.target.classList.add('loading');
});

document.body.addEventListener('htmx:afterRequest', function(evt) {
    evt.target.classList.remove('loading');
});


document.body.addEventListener('htmx:afterSwap', function(event) {
    console.log('After Swap Event details:', event.detail.target.id);
    // Listen for successful deletion
    if (event.detail.target.id === 'delete-product-dialog') {
        console.log('Delete successful');

        setTimeout(() => {
            // Close the dialog
            const dialog = document.getElementById('delete-product-dialog');
            dialog.open = false;
            console.log('Dialog closed');
            // Trigger refresh
            htmx.trigger(document.body, 'refresh-products');
        }, 1000);

    }

    if (event.detail.target.id === 'error-container') {
        console.log('Product form error');
        document.getElementById('error-container').scrollIntoView({behavior: 'smooth'});
        document.querySelector('.error').style.display = 'none';
        document.querySelector('.error-input').classList.remove('error-input');
    }
});
