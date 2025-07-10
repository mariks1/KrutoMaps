let rubrics = [];

async function fetchRubrics() {
    try {
        const response = await fetch('http://localhost:8080/rubrics');
        if (!response.ok) {
            throw new Error('Ошибка сети при загрузке рубрик');
        }
        rubrics = await response.json();
        // Сортировка на фронтенде, если бэкенд не сортирует
        rubrics.sort((a, b) => a.localeCompare(b, 'ru'));
    } catch (error) {
        console.error('Ошибка загрузки рубрик:', error);
        // Можно добавить резервный список или сообщение об ошибке
    }
}

// Объект для хранения данных
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

// Элементы для "Хочу видеть"
const wantToSeeInput = document.getElementById('wantSee');
const wantToSeeSuggestions = document.getElementById('wantSeeSuggestions');
const wantToSeeTagsContainer = document.getElementById('wantSeeTags');

// Элементы для "Не хочу видеть"
const dontWantToSeeInput = document.getElementById('dontWantSee');
const dontWantToSeeSuggestions = document.getElementById('dontWantSeeSuggestions');
const dontWantToSeeTagsContainer = document.getElementById('dontWantSeeTags');

// Элементы для дополнительных настроек
const floorOptionsCheckboxes = document.querySelectorAll('#floorOption .dropdown-content input[type="radio"]');
const placeOptionsCheckboxes = document.querySelectorAll('#placeOption .dropdown-content input[type="checkbox"]');
const submitBtn = document.getElementById('submitBtn');

// Элементы модального окна
const paramsModal = document.getElementById('paramsModal');
const wantSeeModalTags = document.getElementById('wantSeeModalTags');
const dontWantToSeeModalTags = document.getElementById('dontWantSeeModalTags');
const modalClose = document.querySelector('.modal-close');


// Универсальная функция для создания тега
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

// Функция для обновления отображения тегов
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

// Функция для заполнения тегов в модальном окне
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

// Функция для фильтрации и сортировки предложений
function filterSuggestions(input, suggestionsList) {
    const query = input.toLowerCase().trim();
    return suggestionsList.filter(item => item.toLowerCase().includes(query));
}

function showSuggestions(inputElement, suggestionsContainer, array, updateButtonFn, suggestionsList) {
    // Проверка, загружены ли данные
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


// Обработчик для "Хочу видеть"
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

// Обработчик для "Не хочу видеть"
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

// Обработчик для закрытия модального окна через крестик
if (modalClose) {
    modalClose.addEventListener('click', (event) => {
        event.stopPropagation();
        console.log('Modal close clicked');
        paramsModal.style.display = 'none';
    });
} else {
    console.error('modalClose element not found');
}

// Закрытие модального окна при клике вне
paramsModal.addEventListener('click', (event) => {
    if (event.target === paramsModal) {
        console.log('Clicked outside modal');
        paramsModal.style.display = 'none';
    }
});

// Обработчики для выпадающих списков
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

// Инициализация текста кнопок и тегов
updateWantSeeButton();
updateDontWantSeeButton();




// --------ЦЕНА----------------------------------------

// TODO с бекэнда
const MIN_PRICE = 1000;
const MAX_PRICE = 1000000;

function formatNumberWithSpaces(number) {
    return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

// Обработчики для проверки ввода цен
const priceFromInput = document.getElementById('priceFrom');
const priceToInput = document.getElementById('priceTo');


document.addEventListener('DOMContentLoaded', function() {
    const priceFromInput = document.getElementById('priceFrom');
    const priceToInput = document.getElementById('priceTo');

    priceFromInput.placeholder = "от " + formatNumberWithSpaces(MIN_PRICE) + " ₽";
    priceToInput.placeholder = "до " + formatNumberWithSpaces(MAX_PRICE) + " ₽";

    document.getElementById('clearPriceFrom').addEventListener('click', function() {
        priceFromInput.value = '';
        formData.priceFrom = '';
    });

    document.getElementById('clearPriceTo').addEventListener('click', function() {
        priceToInput.value = '';
        formData.priceTo = '';
    });

    fetchRubrics();


});

// Функция для парсинга значения из поля
function parsePriceValue(input) {
    return parseInt(input.value.replace(/[^\d]/g, '')) || 0;
}

// Функция для обновления значения в поле с форматированием
function updatePriceInput(input, value) {
    if (value > 0) {
        input.value = formatNumberWithSpaces(value);
    } else {
        input.value = '';
    }
}

// Обработчик для priceFromInput
priceFromInput.addEventListener('change', function() {
    let valueFrom = parsePriceValue(priceFromInput);
    let valueTo = parsePriceValue(priceToInput);

    // Ограничение для priceFrom
    if (valueFrom < MIN_PRICE) {
        valueFrom = MIN_PRICE;
    } else if (valueFrom > MAX_PRICE) {
        valueFrom = MAX_PRICE;
    }

    // Если priceTo заполнено и priceFrom > priceTo, меняем местами
    if (valueTo > 0 && valueFrom > valueTo) {
        updatePriceInput(priceFromInput, valueTo);
        updatePriceInput(priceToInput, valueFrom);
    } else {
        updatePriceInput(priceFromInput, valueFrom);
    }
});

// Обработчик для priceToInput
priceToInput.addEventListener('change', function() {
    let valueFrom = parsePriceValue(priceFromInput);
    let valueTo = parsePriceValue(priceToInput);

    // Ограничение для priceTo
    if (valueTo < MIN_PRICE) {
        valueTo = MIN_PRICE;
    } else if (valueTo > MAX_PRICE) {
        valueTo = MAX_PRICE;
    }

    // Если priceFrom заполнено и priceFrom > priceTo, меняем местами
    if (valueFrom > 0 && valueFrom > valueTo) {
        updatePriceInput(priceFromInput, valueTo);
        updatePriceInput(priceToInput, valueFrom);
    } else {
        updatePriceInput(priceToInput, valueTo);
    }
});





// --------ПЛОЩАДЬ----------------------------------------

// TODO с бекэнда
const MIN_AREA = 100;
const MAX_AREA = 1000;

// Обработчики для проверки ввода цен
const areaFromInput = document.getElementById('areaFrom');
const areaToInput = document.getElementById('areaTo');


document.addEventListener('DOMContentLoaded', function() {
    const areaFromInput = document.getElementById('areaFrom');
    const areaToInput = document.getElementById('areaTo');

    areaFromInput.placeholder = "от " + formatNumberWithSpaces(MIN_AREA) + " м²";
    areaToInput.placeholder = "до " + formatNumberWithSpaces(MAX_AREA) + " м²";

    document.getElementById('clearAreaFrom').addEventListener('click', function() {
        areaFromInput.value = '';
        formData.priceFrom = '';
    });

    document.getElementById('clearAreaTo').addEventListener('click', function() {
        areaToInput.value = '';
        formData.priceTo = '';
    });


});

// Функция для парсинга значения из поля
function parseAreaValue(input) {
    return parseInt(input.value.replace(/[^\d]/g, '')) || 0;
}

// Функция для обновления значения в поле с форматированием
function updateAreaInput(input, value) {
    if (value > 0) {
        input.value = formatNumberWithSpaces(value);
    } else {
        input.value = '';
    }
}

// Обработчик для areaFromInput
areaFromInput.addEventListener('change', function() {
    let valueFrom = parseAreaValue(areaFromInput);
    let valueTo = parseAreaValue(areaToInput);

    // Ограничение для areaFrom
    if (valueFrom < MIN_AREA) {
        valueFrom = MIN_AREA;
    } else if (valueFrom > MAX_AREA) {
        valueFrom = MAX_AREA;
    }

    // Если areaTo заполнено и areaFrom > areaTo, меняем местами
    if (valueTo > 0 && valueFrom > valueTo) {
        updateAreaInput(areaFromInput, valueTo);
        updateAreaInput(areaToInput, valueFrom);
    } else {
        updateAreaInput(areaFromInput, valueFrom);
    }
});

// Обработчик для areaToInput
areaToInput.addEventListener('change', function() {
    let valueFrom = parseAreaValue(areaFromInput);
    let valueTo = parseAreaValue(areaToInput);

    // Ограничение для areaTo
    if (valueTo < MIN_AREA) {
        valueTo = MIN_AREA;
    } else if (valueTo > MAX_AREA) {
        valueTo = MAX_AREA;
    }

    // Если areaFrom заполнено и areaFrom > areaTo, меняем местами
    if (valueFrom > 0 && valueFrom > valueTo) {
        updateAreaInput(areaFromInput, valueTo);
        updateAreaInput(areaToInput, valueFrom);
    } else {
        updateAreaInput(areaToInput, valueTo);
    }
});