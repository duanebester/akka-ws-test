"use strict";

const ul = document.getElementById("status");
const input = document.querySelector('input[type="file"]');
const uuid = uuidv4();

// Open & setup Websocket
const statusSocket = new WebSocket(
  "ws://localhost:8080/image/" + uuid + "/status"
);
statusSocket.onopen = function() {
  console.log("Websocket opened at " + "/image/" + uuid + "/status");
};
statusSocket.onclose = function() {
  console.log("Websocket closed");
};

// Set function to update DOM on message
statusSocket.onmessage = function(event) {
  console.log(event.data);
  const li = document.createElement("li");
  li.appendChild(document.createTextNode(event.data));
  ul.appendChild(li);
};

input.addEventListener("change", function(e) {
  // Clear any previous DOM updates
  ul.innerHTML = "";

  // Upload image
  const fd = new FormData();
  const file = e.target.files[0];
  fd.append(e.target.name, file, file.name);

  const xhr = new XMLHttpRequest();
  xhr.onload = function() {
    if (xhr.status >= 200 && xhr.status < 300) {
      console.log(xhr.response);
    }
  };

  xhr.open("POST", "/image/" + uuid + "/upload", true);
  xhr.onerror = function(error) {
    console.error(error);
  };
  xhr.send(fd);
});

function uuidv4() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(c) {
    var r = (Math.random() * 16) | 0,
      v = c == "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
