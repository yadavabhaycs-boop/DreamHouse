const categories=[
  {name:"House",icon:"⌂",sub:"Plan a home"},{name:"Room",icon:"▱",sub:"Arrange a room"},
  {name:"Apartment",icon:"▥",sub:"Make space work"},{name:"Shop",icon:"⌑",sub:"Shape your store"},
  {name:"Restaurant",icon:"♨",sub:"Plan for guests"},{name:"Office",icon:"▤",sub:"Organize your team"},
  {name:"Event Hall",icon:"✳",sub:"Plan a gathering"},{name:"Classroom",icon:"▦",sub:"Set up a class"},
  {name:"Garden",icon:"❋",sub:"Arrange a garden"},{name:"Warehouse",icon:"▧",sub:"Organize storage"}
];
const grid=document.querySelector("#category-grid");
const dialog=document.querySelector("#auth-dialog");
let signedIn=false;
const welcomeName=new URLSearchParams(location.search).get("welcome");

function rememberedName(){return sessionStorage.getItem("dreamhouse.userName")||"";}
function renderNavigation(active,name=""){
  signedIn=active;
  const guest=document.querySelector("#guest-nav");
  const member=document.querySelector("#member-nav");
  const memberName=document.querySelector("#member-name");
  const explore=document.querySelector("#explore-link");
  const hint=document.querySelector("#category-hint");
  if(guest)guest.hidden=active;
  if(member)member.hidden=!active;
  if(explore)explore.hidden=active;
  if(memberName&&active)memberName.textContent=name||"User";
  if(hint)hint.textContent=active?"Choose a category to open its layout planner.":"Choose a category to begin. Log in or create an account to generate layouts.";
}
async function getSession(){
  try{
    const response=await fetch("auth/status",{headers:{Accept:"application/json"},cache:"no-store"});
    if(!response.ok)return {available:false,authenticated:false};
    return {...await response.json(),available:true};
  }catch{return {available:false,authenticated:false};}
}
async function refreshNavigation(){
  const saved=rememberedName();
  if(saved)renderNavigation(true,saved);
  const session=await getSession();
  if(session.authenticated===true){
    const name=session.name||saved||"User";
    sessionStorage.setItem("dreamhouse.userName",name);
    renderNavigation(true,name);
    return;
  }
  if(session.available&&!saved){renderNavigation(false);}
}

if(welcomeName){sessionStorage.setItem("dreamhouse.userName",welcomeName);history.replaceState({},document.title,location.pathname+location.hash);}

if(grid&&dialog){
  for(const category of categories){
    const card=document.createElement("button");
    card.type="button";
    card.className="category-card";
    card.setAttribute("aria-label",`${category.name}: choose this space`);
    card.innerHTML=`<span class="category-icon" aria-hidden="true">${category.icon}</span><span class="category-name">${category.name}</span><span class="category-sub">${category.sub}</span><span class="category-arrow" aria-hidden="true">↗</span>`;
    card.addEventListener("click",async()=>{
      sessionStorage.setItem("dreamhouse.selectedCategory",category.name);
      if(signedIn)location.href=`generator.html?category=${encodeURIComponent(category.name)}`;
      else dialog.showModal();
    });
    grid.append(card);
  }
  const more=document.createElement("article");
  more.className="category-card coming-soon";
  more.innerHTML='<span class="category-icon" aria-hidden="true">✦</span><span class="category-name">More Categories</span><span class="category-sub">Coming soon</span>';
  grid.append(more);
  document.querySelectorAll("[data-open-auth]").forEach(button=>button.addEventListener("click",()=>dialog.showModal()));
  document.querySelectorAll("[data-close-auth]").forEach(button=>button.addEventListener("click",()=>dialog.close()));
  dialog.addEventListener("click",event=>{if(event.target===dialog)dialog.close();});
}

refreshNavigation();
window.addEventListener("pageshow",refreshNavigation);
