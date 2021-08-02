const lecturerRadioButton = document.querySelector("#lecturer-radio-id");
const groupRadioButton = document.querySelector("#group-radio-id");

const weekRadioButton = document.querySelector("#week-radio-id");
const monthRadioButton = document.querySelector("#month-radio-id");

let inputPeriodDiv = document.querySelector("#input-period");

let periodField = document.querySelector("#period-id");

let monthField = document.createElement("input");
monthField.setAttribute("class", "form-control");
monthField.setAttribute("type", "month");
monthField.setAttribute("name", "month-value");
monthField.setAttribute("required", "required");

lecturerRadioButton.onclick = function () {
    let defaultPepleRoleField = document.querySelector("#default-people-role-field-id");
    defaultPepleRoleField.classList.add("visually-hidden");
    
    let groupSelect = document.querySelector("#group-field-id");
    groupSelect.classList.add("visually-hidden");
    groupSelect.removeAttribute("required");
    groupSelect.value = document.querySelector("#default-group-option").text;
    
    let lecturerSelect = document.querySelector("#lecturer-field-id");
    lecturerSelect.classList.remove("visually-hidden");
    lecturerSelect.setAttribute("required", "required");
}

groupRadioButton.onclick = function () {
    let defaultPepleRoleField = document.querySelector("#default-people-role-field-id");
    defaultPepleRoleField.classList.add("visually-hidden");
    
    let lecturerSelect = document.querySelector("#lecturer-field-id");
    lecturerSelect.classList.add("visually-hidden");
    lecturerSelect.removeAttribute("required");
    lecturerSelect.value = document.querySelector("#default-lecturer-option").text;;
    
    let groupSelect = document.querySelector("#group-field-id");
    groupSelect.classList.remove("visually-hidden");
    groupSelect.setAttribute("required", "required");
}

weekRadioButton.onclick = function () {
    while(inputPeriodDiv.firstChild) {
        inputPeriodDiv.removeChild(inputPeriodDiv.firstChild);
    }
    
    monthField.value="";
    periodField.setAttribute("placeholder", "Nothing to type, just hit on the button below.");
    inputPeriodDiv.appendChild(periodField);
}

monthRadioButton.onclick = function () {
    while(inputPeriodDiv.firstChild) {
        inputPeriodDiv.removeChild(inputPeriodDiv.firstChild);
    }
    
    inputPeriodDiv.appendChild(monthField);
}