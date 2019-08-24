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
  
    // Set DELETE request if delete button pressed
    $("#confirmDelete").click(function () {
      $('input[name="_method"]').attr('value', 'delete');
      $('form').submit();
    });
  
  });