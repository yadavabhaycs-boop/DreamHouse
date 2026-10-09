const message=document.querySelector("#form-message");
const query=new URLSearchParams(location.search);
if(query.has("loggedOut"))sessionStorage.removeItem("dreamhouse.userName");
const messages={details:"Enter a valid name and email address.",password:"Passwords must match and contain at least 8 characters.",exists:"An account with this email already exists. Try logging in.",invalid:"Email, password, or account type is incorrect.",database:"DreamHouse could not connect to MySQL. Check the local database configuration.",driver:"MySQL Connector/J is missing from the deployed app. In NetBeans, Clean and Build the project, then run it on Tomcat again.","mysql-auth":"MySQL authentication needs an updated connection setting. Clean and Build DreamHouse, then run it again from NetBeans."};
if(message){const error=query.get("error");if(error&&messages[error]){message.textContent=messages[error];message.hidden=false;}else if(query.has("registered")){message.textContent="Account created. You can log in now.";message.dataset.kind="success";message.hidden=false;}else if(query.has("loggedOut")){message.textContent="You are logged out.";message.dataset.kind="success";message.hidden=false;}}
const category=document.querySelector("#selected-category");
if(category)category.value=sessionStorage.getItem("dreamhouse.selectedCategory")||query.get("category")||"";
const form=document.querySelector("#register-form");
if(form)form.addEventListener("submit",event=>{if(form.elements.password.value!==form.elements.confirmPassword.value){event.preventDefault();if(message){message.textContent=messages.password;message.hidden=false;}}});




async function redirectSignedInUser(){
  try{
    const response=await fetch("auth/status",{headers:{Accept:"application/json"},cache:"no-store"});
    if(!response.ok)return;
    const session=await response.json();
    if(!session.authenticated)return;
    location.replace("index.html");
  }catch{}
}
redirectSignedInUser();
window.addEventListener("pageshow",redirectSignedInUser);
