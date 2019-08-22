function deleteFile(values) {
    url = "/storage/deleteFile";
    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    xhr.send(values);
    
    xhr.onreadystatechange = function() {
      if (xhr.readyState == 4 && xhr.status == 200) {
          window.location = "/index";
      }
    }
  };

$('#deleteBtn').submit(function () {
    $('html,body, button').css('cursor','wait');
    var values = $("form").serialize();
    console.log(values);
    deleteFile(values);
    }
);
  
$(document).ready(function () {
    // Disable form action
  $('#editForm').submit(function(event) {
    event.preventDefault();  
  });
  
    // Populate drop-down menus
    window.fillSelectOptionsWithNumberRange("min-players", minPlayers, 1, 200)
    window.fillSelectOptionsWithNumberRange("max-players", maxPlayers, 1, 200)
    window.fillSelectOptionsWithNumberRange("age", minAge, 1, 18)
  
    // Generate category checkboxes
    var addedCategoriesRow = '';
    for (var key in categoryDict) {
      var catName = categoryDict[key];
      addedCategoriesRow += '<div class="form-check form-check-inline"><input class="form-check-input" type="checkbox" id="' + catName + '" value="' + key + '"><label class="form-check-label modal-label" for="' + catName + '">' + catName + '</label></div>';
    }
    $('#categoryModalBody').html(addedCategoriesRow);
  
    // Generate mechanic checkboxes
    var addedMechanicsRow = '';
    for (var key in mechanicDict) {
      var mechName = mechanicDict[key];
      addedMechanicsRow += '<div class="form-check form-check-inline"><input class="form-check-input" type="checkbox" id="' + mechName + '" value="' + key + '"><label class="form-check-label modal-label" for="' + mechName + '">' + mechName + '</label></div>';
    }
    $('#mechanicModalBody').html(addedMechanicsRow);
  
    // Mark appropriate categories as checked
    setCheckedCategories();
    setCheckedMechanics();
  
    // Set DELETE request if delete button pressed
    $("#confirmDelete").click(function () {
      $('input[name="_method"]').attr('value', 'delete');
      $('form').submit();
    });
  
  });