async function loadProfile(){
  try{
    const response=await fetch("auth/status",{headers:{Accept:"application/json"},cache:"no-store",credentials:"include"});
    const session=await response.json();
    if(!response.ok||session.authenticated!==true){location.href="login.html";return;}
    document.querySelector("#profile-name").textContent=session.name||"DreamHouse user";
    document.querySelector("#profile-email").textContent=session.email||"Not available";
    document.querySelector("#profile-role").textContent=session.role==="ADMIN"?"Administrator":"User";
    if(session.name){
      sessionStorage.setItem("dreamhouse.userName",session.name);
      document.querySelectorAll(".user-display-name").forEach(el=>el.textContent=session.name);
    }
  }catch{location.href="login.html";}
}
const cached=sessionStorage.getItem("dreamhouse.userName")||"";
if(cached){document.querySelectorAll(".user-display-name").forEach(el=>el.textContent=cached);}
loadProfile();
