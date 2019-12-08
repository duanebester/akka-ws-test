"use strict";
function uuidv4() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(c) {
    var r = (Math.random() * 16) | 0,
      v = c == "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

const ul = document.getElementById("status");
const input = document.querySelector('input[type="file"]');

input.addEventListener("change", function(e) {
  // Generate UUID
  const uuid = uuidv4();
  // Clear any previous DOM updates
  ul.innerHTML = "";

  // Open & setup Websocket
  const statusSocket = new WebSocket(
    "ws://localhost:8080/image/" + uuid + "/status"
  );
  statusSocket.onopen = function() {
    console.log("Websocket opened");
  };
  statusSocket.onclose = function() {
    console.log("Websocket closed");
  };

  // Set function to update DOM on message
  statusSocket.onmessage = function(event) {
    const li = document.createElement("li");
    li.appendChild(document.createTextNode(event.data));
    ul.appendChild(li);
  };

  // Upload image
  const fd = new FormData();
  const file = e.target.files[0];
  fd.append(e.target.name, file, file.name);

  const xhr = new XMLHttpRequest();
  xhr.onload = function() {
    if (xhr.status >= 200 && xhr.status < 300) {
      console.log(xhr.response);
    }
    // Close Socket
    statusSocket.close();
  };

  xhr.open("POST", "/image/" + uuid + "/upload", true);
  xhr.onerror = function(error) {
    console.error(error);
    // Close Socket
    statusSocket.close();
  };
  xhr.send(fd);
});
