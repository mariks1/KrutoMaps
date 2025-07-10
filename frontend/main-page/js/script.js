let rubrics = [];
let MIN_PRICE = 0;
let MAX_PRICE = 0;
let MIN_AREA = 0;
let MAX_AREA = 0;


async function fetchRubrics() {
    try {
        const response = await fetch('http://localhost:8080/rubrics');
        if (!response.ok) {
            throw new Error('Ошибка сети при загрузке рубрик');
        }
        rubrics = await response.json();
        rubrics.sort((a, b) => a.localeCompare(b, 'ru'));
    } catch (error) {
        console.error('Ошибка загрузки рубрик:', error);
    }
}

async function fetchPrice() {
    try {
        const response = await fetch('http://localhost:8080/price-range');
        if (!response.ok) {
            throw new Error('Ошибка сети при загрузке диапазона цен');
        }
        const priceRange = await response.json();
        MIN_PRICE = priceRange.minPrice || 0;
        MAX_PRICE = priceRange.maxPrice || 0;

        priceFromInput.placeholder = `от ${formatNumberWithSpaces(MIN_PRICE)} ₽`;
        priceToInput.placeholder = `до ${formatNumberWithSpaces(MAX_PRICE)} ₽`;
    } catch (error) {
        console.error('Ошибка загрузки диапазона цен:', error);
        MIN_PRICE = 1000;
        MAX_PRICE = 1000000;
        priceFromInput.placeholder = `от ${formatNumberWithSpaces(MIN_PRICE)} ₽`;
        priceToInput.placeholder = `до ${formatNumberWithSpaces(MAX_PRICE)} ₽`;
    }
}

async function fetchArea() {
    try {
        const response = await fetch('http://localhost:8080/area-range');
        if (!response.ok) {
            throw new Error('Ошибка сети при загрузке диапазона площадей');
        }
        const areaRange = await response.json();
        MIN_AREA = areaRange.minArea || 0;
        MAX_AREA = areaRange.maxArea || 0;

        areaFromInput.placeholder = `от ${formatNumberWithSpaces(MIN_AREA)} м²`;
        areaToInput.placeholder = `до ${formatNumberWithSpaces(MAX_AREA)} м²`;
    } catch (error) {
        console.error('Ошибка загрузки диапазона площадей:', error);
        MIN_AREA = 100;
        MAX_AREA = 1000;
        areaFromInput.placeholder = `от ${formatNumberWithSpaces(MIN_AREA)} м²`;
        areaToInput.placeholder = `до ${formatNumberWithSpaces(MAX_AREA)} м²`;
    }
}

const formData = {
    wantToSeeArray: [],
    dontWantToSeeArray: [],
    priceFrom: '',
    priceTo: '',
    floorOptions: [],
    placeOptions: [],
    areaFrom: '',
    areaTo: ''
};

const wantToSeeInput = document.getElementById('wantSee');
const wantToSeeSuggestions = document.getElementById('wantSeeSuggestions');
const wantToSeeTagsContainer = document.getElementById('wantSeeTags');

const dontWantToSeeInput = document.getElementById('dontWantSee');
const dontWantToSeeSuggestions = document.getElementById('dontWantSeeSuggestions');
const dontWantToSeeTagsContainer = document.getElementById('dontWantSeeTags');

const floorOptionsCheckboxes = document.querySelectorAll('#floorOption .dropdown-content input[type="radio"]');
const placeOptionsCheckboxes = document.querySelectorAll('#placeOption .dropdown-content input[type="checkbox"]');
const submitBtn = document.getElementById('submitBtn');

const paramsModal = document.getElementById('paramsModal');
const wantSeeModalTags = document.getElementById('wantSeeModalTags');
const dontWantToSeeModalTags = document.getElementById('dontWantSeeModalTags');
const modalClose = document.querySelector('.modal-close');


function createTag(text, container, array, updateButtonFn) {
    const tag = document.createElement('div');
    tag.className = 'tag';
    
    const tagText = document.createElement('span');
    tagText.textContent = text;
    
    const removeBtn = document.createElement('button');
    removeBtn.className = 'remove-btn';
    removeBtn.textContent = '×';
    
    removeBtn.addEventListener('click', (event) => {
        event.stopPropagation();
        tag.remove();
        const index = array.indexOf(text);
        if (index > -1) {
            array.splice(index, 1);
            updateButtonFn();
            updateTagsDisplay(container === wantSeeModalTags ? wantToSeeTagsContainer : dontWantToSeeTagsContainer, array, updateButtonFn);
            populateModalTags();
        }
    });
    
    tag.appendChild(tagText);
    tag.appendChild(removeBtn);
    container.appendChild(tag);
}

function updateTagsDisplay(container, array, updateButtonFn) {
    container.innerHTML = '';
    if (array.length <= 3) {
        array.forEach(text => createTag(text, container, array, updateButtonFn));
    } else {
        array.slice(0, 3).forEach(text => createTag(text, container, array, updateButtonFn));
        const moreBtn = document.createElement('button');
        moreBtn.className = 'more-btn';
        moreBtn.textContent = '...';
        moreBtn.addEventListener('click', (event) => {
            event.stopPropagation();
            paramsModal.style.display = 'flex';
            populateModalTags();
        });
        container.appendChild(moreBtn);
    }
}

function populateModalTags() {
    wantSeeModalTags.innerHTML = '';
    dontWantToSeeModalTags.innerHTML = '';
    
    formData.wantToSeeArray.forEach(text => {
        createTag(text, wantSeeModalTags, formData.wantToSeeArray, updateWantSeeButton);
    });
    
    formData.dontWantToSeeArray.forEach(text => {
        createTag(text, dontWantToSeeModalTags, formData.dontWantToSeeArray, updateDontWantSeeButton);
    });
}

function filterSuggestions(input, suggestionsList) {
    const query = input.toLowerCase().trim();
    return suggestionsList.filter(item => item.toLowerCase().includes(query));
}

function showSuggestions(inputElement, suggestionsContainer, array, updateButtonFn, suggestionsList) {
    if (suggestionsList.length === 0) {
        suggestionsContainer.innerHTML = '<div>Загрузка...</div>';
        suggestionsContainer.style.display = 'block';
        return;
    }

    const query = inputElement.value;
    suggestionsContainer.innerHTML = '';
    
    const filteredSuggestions = query.trim() === ''
        ? suggestionsList.filter(item => !array.includes(item))
        : filterSuggestions(query, suggestionsList).filter(item => !array.includes(item));
    
    if (filteredSuggestions.length > 0) {
        filteredSuggestions.forEach(suggestion => {
            const suggestionItem = document.createElement('div');
            suggestionItem.textContent = suggestion;
            suggestionItem.addEventListener('click', () => {
                if (!array.includes(suggestion)) {
                    array.push(suggestion);
                    updateTagsDisplay(array === formData.wantToSeeArray ? wantToSeeTagsContainer : dontWantToSeeTagsContainer, array, updateButtonFn);
                    inputElement.value = '';
                    suggestionsContainer.style.display = 'none';
                }
            });
            suggestionsContainer.appendChild(suggestionItem);
        });
        suggestionsContainer.style.display = 'block';
    } else {
        suggestionsContainer.style.display = 'none';
    }
}

function updateWantSeeButton() {
    updateTagsDisplay(wantToSeeTagsContainer, formData.wantToSeeArray, updateWantSeeButton);
}

wantToSeeInput.addEventListener('input', () => {
    showSuggestions(wantToSeeInput, wantToSeeSuggestions, formData.wantToSeeArray, updateWantSeeButton, rubrics);
});

wantToSeeInput.addEventListener('focus', () => {
    showSuggestions(wantToSeeInput, wantToSeeSuggestions, formData.wantToSeeArray, updateWantSeeButton, rubrics);
});

wantToSeeInput.addEventListener('blur', () => {
    setTimeout(() => {
        wantToSeeSuggestions.style.display = 'none';
    }, 200);
});

function updateDontWantSeeButton() {
    updateTagsDisplay(dontWantToSeeTagsContainer, formData.dontWantToSeeArray, updateDontWantSeeButton);
}

dontWantToSeeInput.addEventListener('input', () => {
    showSuggestions(dontWantToSeeInput, dontWantToSeeSuggestions, formData.dontWantToSeeArray, updateDontWantSeeButton, rubrics);
});

dontWantToSeeInput.addEventListener('focus', () => {
    showSuggestions(dontWantToSeeInput, dontWantToSeeSuggestions, formData.dontWantToSeeArray, updateDontWantSeeButton, rubrics);
});

dontWantToSeeInput.addEventListener('blur', () => {
    setTimeout(() => {
        dontWantToSeeSuggestions.style.display = 'none';
    }, 200);
});

if (modalClose) {
    modalClose.addEventListener('click', (event) => {
        event.stopPropagation();
        console.log('Modal close clicked');
        paramsModal.style.display = 'none';
    });
} else {
    console.error('modalClose element not found');
}

paramsModal.addEventListener('click', (event) => {
    if (event.target === paramsModal) {
        console.log('Clicked outside modal');
        paramsModal.style.display = 'none';
    }
});

document.getElementById('dropdown1').querySelectorAll('input[type="radio"]').forEach(function(radio) {
    radio.addEventListener('change', function() {
        const dropdown = this.closest('.dropdown');
        const dropbtn = dropdown.querySelector('.dropbtn_selected');
        dropbtn.textContent = this.value;
    });
});

document.getElementById('dropdown2').querySelectorAll('input[type="checkbox"]').forEach(function(checkbox) {
    checkbox.addEventListener('change', function() {
        const dropdown = this.closest('.dropdown');
        const checkboxes = dropdown.querySelectorAll('.dropdown-content input[type="checkbox"]');
        const allCheckboxes = Array.from(checkboxes);
        const anyCheckbox = dropdown.querySelector('input[value="Любой"]');

        if (this.value === 'Любой' && this.checked) {
            allCheckboxes.forEach(cb => {
                if (cb.value !== 'Любой') cb.checked = false;
            });
        }

        const anyChecked = allCheckboxes.filter(cb => cb.checked && cb.value !== 'Любой');
        if (anyChecked.length > 0) {
            anyCheckbox.checked = false;
        }

        const selectedOptions = allCheckboxes.filter(cb => cb.checked).map(cb => cb.value);
        const dropbtn = dropdown.querySelector('.dropbtn_selected');
        dropbtn.textContent = selectedOptions.length > 0 ? selectedOptions.join(', ') : 'Любой';
    });
});

function toggleDropdown(dropdownId) {
    const dropdown = document.getElementById(dropdownId);
    const dropdownContent = dropdown.querySelector('.dropdown-content');
    dropdownContent.style.display = dropdownContent.style.display === 'block' ? 'none' : 'block';
}

document.getElementById('dropdown1Btn').addEventListener('click', function(event) {
    event.preventDefault();
    toggleDropdown('dropdown1');
});

document.getElementById('dropdown2Btn').addEventListener('click', function(event) {
    event.preventDefault();
    toggleDropdown('dropdown2');
});

document.addEventListener('click', function(event) {
    const dropdowns = document.getElementsByClassName('dropdown');
    for (let i = 0; i < dropdowns.length; i++) {
        const dropdownContent = dropdowns[i].querySelector('.dropdown-content');
        if (!dropdowns[i].contains(event.target)) {
            dropdownContent.style.display = 'none';
        }
    }
});

updateWantSeeButton();
updateDontWantSeeButton();


// --------ЦЕНА----------------------------------------

function formatNumberWithSpaces(number) {
    return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

const priceFromInput = document.getElementById('priceFrom');
const priceToInput = document.getElementById('priceTo');


document.addEventListener('DOMContentLoaded', function() {
    const priceFromInput = document.getElementById('priceFrom');
    const priceToInput = document.getElementById('priceTo');

    document.getElementById('clearPriceFrom').addEventListener('click', function() {
        priceFromInput.value = '';
        formData.priceFrom = '';
    });

    document.getElementById('clearPriceTo').addEventListener('click', function() {
        priceToInput.value = '';
        formData.priceTo = '';
    });

    document.getElementById('clearAreaFrom').addEventListener('click', function() {
        areaFromInput.value = '';
        formData.priceFrom = '';
    });

    document.getElementById('clearAreaTo').addEventListener('click', function() {
        areaToInput.value = '';
        formData.priceTo = '';
    });

    fetchRubrics();
    fetchPrice();
    fetchArea();

});

function parsePriceValue(input) {
    return parseInt(input.value.replace(/[^\d]/g, '')) || 0;
}

function updatePriceInput(input, value) {
    if (value > 0) {
        input.value = formatNumberWithSpaces(value);
    } else {
        input.value = '';
    }
}

priceFromInput.addEventListener('change', function() {
    let valueFrom = parsePriceValue(priceFromInput);
    let valueTo = parsePriceValue(priceToInput);

    if (valueFrom < MIN_PRICE) {
        valueFrom = MIN_PRICE;
    } else if (valueFrom > MAX_PRICE) {
        valueFrom = MAX_PRICE;
    }

    if (valueTo > 0 && valueFrom > valueTo) {
        updatePriceInput(priceFromInput, valueTo);
        updatePriceInput(priceToInput, valueFrom);
    } else {
        updatePriceInput(priceFromInput, valueFrom);
    }
    formData.priceFrom = valueFrom;
});

priceToInput.addEventListener('change', function() {
    let valueFrom = parsePriceValue(priceFromInput);
    let valueTo = parsePriceValue(priceToInput);

    if (valueTo < MIN_PRICE) {
        valueTo = MIN_PRICE;
    } else if (valueTo > MAX_PRICE) {
        valueTo = MAX_PRICE;
    }

    if (valueFrom > 0 && valueFrom > valueTo) {
        updatePriceInput(priceFromInput, valueTo);
        updatePriceInput(priceToInput, valueFrom);
    } else {
        updatePriceInput(priceToInput, valueTo);
    }
    formData.priceTo = valueTo;
});

// --------ПЛОЩАДЬ----------------------------------------

const areaFromInput = document.getElementById('areaFrom');
const areaToInput = document.getElementById('areaTo');

function parseAreaValue(input) {
    return parseInt(input.value.replace(/[^\d]/g, '')) || 0;
}

function updateAreaInput(input, value) {
    if (value > 0) {
        input.value = formatNumberWithSpaces(value);
    } else {
        input.value = '';
    }
}

areaFromInput.addEventListener('change', function() {
    let valueFrom = parseAreaValue(areaFromInput);
    let valueTo = parseAreaValue(areaToInput);

    if (valueFrom < MIN_AREA) {
        valueFrom = MIN_AREA;
    } else if (valueFrom > MAX_AREA) {
        valueFrom = MAX_AREA;
    }

    if (valueTo > 0 && valueFrom > valueTo) {
        updateAreaInput(areaFromInput, valueTo);
        updateAreaInput(areaToInput, valueFrom);
    } else {
        updateAreaInput(areaFromInput, valueFrom);
    }
});

areaToInput.addEventListener('change', function() {
    let valueFrom = parseAreaValue(areaFromInput);
    let valueTo = parseAreaValue(areaToInput);

    if (valueTo < MIN_AREA) {
        valueTo = MIN_AREA;
    } else if (valueTo > MAX_AREA) {
        valueTo = MAX_AREA;
    }

    if (valueFrom > 0 && valueFrom > valueTo) {
        updateAreaInput(areaFromInput, valueTo);
        updateAreaInput(areaToInput, valueFrom);
    } else {
        updateAreaInput(areaToInput, valueTo);
    }
});

async function sendData() {
    const wantToSeeArray = formData.wantToSeeArray;
    const dontWantToSeeArray = formData.dontWantToSeeArray;
    const priceFrom = parsePriceValue(priceFromInput);
    const priceTo = parsePriceValue(priceToInput);
    const floorOption = document.querySelector('input[name="floorOption"]:checked').value;
    const placeOptions = Array.from(document.querySelectorAll('#placeOption .dropdown-content input[type="checkbox"]:checked')).map(cb => cb.value);
    const areaFrom = parseAreaValue(areaFromInput);
    const areaTo = parseAreaValue(areaToInput);

    const data = {
        wantToSee: wantToSeeArray,
        dontWantToSee: dontWantToSeeArray,
        priceFrom: priceFrom,
        priceTo: priceTo,
        floorOption: floorOption,
        placeOptions: placeOptions,
        areaFrom: areaFrom,
        areaTo: areaTo
    };

    try {
        const response = await fetch('http://localhost:8080/select-realty', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error('Ошибка сети при отправке запроса');
        }

        const result = await response.text();
        console.log('Ответ от бэкенда:', result);
    } catch (error) {
        console.error('Ошибка:', error);
    }
}