const grid=document.querySelector("#saved-design-grid");
const statusLine=document.querySelector("#saved-status");
const empty=document.querySelector("#saved-empty");
const roomLayouts=[
  {bed:[5,30,43,49],wardrobe:[69,6,25,18],desk:[58,70,34,18],tv:[38,7,25,13],sofa:[54,38,32,18],path:[42,48,18,17]},
  {bed:[52,29,43,49],wardrobe:[6,6,25,18],desk:[8,70,34,18],tv:[37,7,25,13],sofa:[13,38,32,18],path:[41,48,18,17]},
  {bed:[23,5,54,38],wardrobe:[5,64,25,18],desk:[66,65,29,18],tv:[38,48,25,13],sofa:[7,43,28,17],path:[41,40,18,16]},
  {bed:[50,54,44,39],wardrobe:[5,5,25,18],desk:[58,7,35,18],tv:[37,34,25,13],sofa:[8,39,30,18],path:[39,34,17,17]},
  {bed:[5,6,44,45],wardrobe:[67,6,27,18],desk:[65,72,30,18],tv:[37,57,25,13],sofa:[10,60,30,18],path:[45,48,15,17]},
  {bed:[28,55,45,38],wardrobe:[7,8,24,18],desk:[66,8,28,18],tv:[38,38,25,13],sofa:[7,66,27,17],path:[43,39,16,14]}
];
function kindOf(label){
  const value=label.toLowerCase();
  if(/bed/.test(value))return"bed";if(/wardrobe|closet/.test(value))return"wardrobe";if(/bath|washroom|toilet/.test(value))return"bath";
  if(/kitchen/.test(value))return"kitchen";if(/tv/.test(value))return"tv";if(/sofa|living|lounge/.test(value))return"sofa";
  if(/study|desk|work area|cabin|reception|teacher|office/.test(value))return"desk";if(/dining|table|seating|guest|student desks/.test(value))return"dining";
  if(/garden|plant|lawn|water feature/.test(value))return"plant";if(/rack|storage|warehouse|loading|packing|aisle/.test(value))return"shelves";
  if(/parking/.test(value))return"car";if(/stage|dj|entertainment/.test(value))return"stage";if(/circulation|pathway/.test(value))return"path";return"table";
}
function renderPlan(design){
  const plan=document.createElement("div");plan.className=`floor-plan${design.category==="Room"?" room-plan":""}`;plan.style.setProperty("--plan-ratio",`${design.width}/${design.length}`);
  if(design.category==="Room"){
    const variant=Math.max(0,Math.min(5,Number((design.name.match(/^Design (\d+)/)||[])[1]||1)-1));const layout=roomLayouts[variant];
    design.items.forEach(item=>{const kind=kindOf(item.label),p=layout[kind]||layout.path;const piece=document.createElement("div");piece.className=`room-fixture fixture-${kind}`;piece.style.left=`${p[0]}%`;piece.style.top=`${p[1]}%`;piece.style.width=`${p[2]}%`;piece.style.height=`${p[3]}%`;const drawing=document.createElement("span");drawing.className="fixture-drawing";drawing.setAttribute("aria-hidden","true");const label=document.createElement("span");label.className="fixture-label";label.textContent=item.label;piece.append(drawing,label);plan.append(piece);});
  }else{
    design.items.forEach(item=>{const zone=document.createElement("div");zone.className=`plan-zone zone-${kindOf(item.label)}`;zone.style.left=`${item.x}%`;zone.style.top=`${item.y}%`;zone.style.width=`${item.width}%`;zone.style.height=`${item.height}%`;const label=document.createElement("span");label.className="zone-label";label.textContent=item.label;const fixture=document.createElement("span");fixture.className="zone-furniture";fixture.setAttribute("aria-hidden","true");zone.append(label,fixture);plan.append(zone);});
  }
  const north=document.createElement("span");north.className="plan-north";north.textContent="N ↑";const entrance=document.createElement("span");entrance.className="plan-entry";entrance.textContent="ENTRY";const width=document.createElement("span");width.className="plan-dimension plan-dimension-top";width.textContent=`${design.width} ft`;const length=document.createElement("span");length.className="plan-dimension plan-dimension-side";length.textContent=`${design.length} ft`;const windowTop=document.createElement("span");windowTop.className="plan-window plan-window-top window-v1";const windowSide=document.createElement("span");windowSide.className="plan-window plan-window-side window-v1";plan.append(windowTop,windowSide,north,entrance,width,length);
  return plan;
}
function makeCard(design){
  const card=document.createElement("article");card.className="design-card saved-design-card";
  const header=document.createElement("header");const title=document.createElement("h2");title.textContent=design.name;const description=document.createElement("p");description.textContent=design.description;const meta=document.createElement("p");meta.className="saved-meta";meta.textContent=`${design.category} · ${design.width} ft × ${design.length} ft · Saved ${new Date(String(design.createdAt).replace(" ","T")).toLocaleString()}`;header.append(title,description,meta);
  const wrap=document.createElement("div");wrap.className="plan-wrap";wrap.append(renderPlan(design));
  const actions=document.createElement("div");actions.className="design-actions";const remove=document.createElement("button");remove.type="button";remove.className="button button-outline delete-design-button";remove.textContent="Remove saved design";remove.addEventListener("click",()=>deleteDesign(design.id,card,remove));actions.append(remove);card.append(header,wrap,actions);return card;
}
async function loadSaved(){
  try{const response=await fetch("layout/saved",{headers:{Accept:"application/json"},cache:"no-store"});const data=await response.json();if(response.status===401){location.href="login.html";return;}if(!response.ok)throw new Error(data.error||"Could not load saved designs.");grid.replaceChildren(...data.designs.map(makeCard));const count=data.designs.length;statusLine.textContent=count?`${count} saved ${count===1?"design":"designs"}`:"";empty.hidden=count!==0;}
  catch(error){statusLine.textContent=error.message||"Could not load your saved designs.";}
}
async function deleteDesign(id,card,button){
  if(!confirm("Remove this saved design from your collection?"))return;button.disabled=true;button.textContent="Removing…";
  try{const response=await fetch(`layout/saved?id=${encodeURIComponent(id)}`,{method:"DELETE",headers:{Accept:"application/json"}});const data=await response.json();if(!response.ok)throw new Error(data.error||"Could not remove this design.");card.remove();const remaining=grid.querySelectorAll(".saved-design-card").length;statusLine.textContent=remaining?`${remaining} saved ${remaining===1?"design":"designs"}`:"";empty.hidden=remaining!==0;}
  catch(error){button.disabled=false;button.textContent="Remove saved design";statusLine.textContent=error.message||"Could not remove this design.";}
}
loadSaved();
