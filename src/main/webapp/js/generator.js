const form=document.querySelector("#generator-form");
const categorySelect=document.querySelector("#category");
const optionsHost=document.querySelector("#category-options");
const message=document.querySelector("#generator-message");
const button=document.querySelector("#generate-button");
const results=document.querySelector("#results");
const designGrid=document.querySelector("#design-grid");
const summary=document.querySelector("#results-summary");
let lastGenerationParams=null;

const options={
  House:[{type:"number",name:"bedrooms",label:"Bedrooms",min:1,max:8,value:3},{type:"number",name:"bathrooms",label:"Bathrooms",min:0,max:6,value:2},{type:"check",name:"kitchen",label:"Kitchen",checked:true},{type:"check",name:"living",label:"Living room",checked:true},{type:"check",name:"dining",label:"Dining area",checked:true},{type:"check",name:"parking",label:"Parking",checked:false}],
  Apartment:[{type:"number",name:"bedrooms",label:"Bedrooms",min:1,max:8,value:2},{type:"number",name:"bathrooms",label:"Bathrooms",min:0,max:6,value:1},{type:"check",name:"kitchen",label:"Kitchen",checked:true},{type:"check",name:"living",label:"Living room",checked:true},{type:"check",name:"dining",label:"Dining area",checked:true},{type:"check",name:"balcony",label:"Balcony",checked:true}],
  Room:[{type:"check",name:"bed",label:"Bed",checked:true},{type:"check",name:"wardrobe",label:"Wardrobe",checked:true},{type:"check",name:"studyTable",label:"Study table",checked:true},{type:"check",name:"sofa",label:"Sofa",checked:false},{type:"check",name:"tv",label:"TV unit",checked:true}],
  Shop:[{type:"number",name:"racks",label:"Display racks",min:0,max:12,value:4},{type:"check",name:"storage",label:"Storage",checked:true},{type:"check",name:"billing",label:"Billing counter",checked:true},{type:"check",name:"display",label:"Display area",checked:true},{type:"check",name:"entrance",label:"Entrance",checked:true}],
  Restaurant:[{type:"number",name:"seats",label:"Seats",min:4,max:300,value:24},{type:"number",name:"tables",label:"Tables",min:1,max:40,value:6},{type:"check",name:"kitchen",label:"Kitchen",checked:true},{type:"check",name:"counter",label:"Service counter",checked:true},{type:"check",name:"washroom",label:"Washroom",checked:true},{type:"check",name:"entrance",label:"Entrance",checked:true}],
  Office:[{type:"number",name:"employees",label:"Employees",min:1,max:100,value:8},{type:"number",name:"cabins",label:"Cabins",min:0,max:12,value:2},{type:"check",name:"meeting",label:"Meeting room",checked:true},{type:"check",name:"reception",label:"Reception",checked:true},{type:"check",name:"pantry",label:"Pantry",checked:false}],
  "Event Hall":[{type:"number",name:"guests",label:"Guest capacity",min:10,max:1000,value:100},{type:"check",name:"stage",label:"Stage",checked:true},{type:"check",name:"dining",label:"Dining area",checked:true},{type:"check",name:"dj",label:"DJ / entertainment",checked:true},{type:"check",name:"entrance",label:"Entrance",checked:true}],
  Classroom:[{type:"number",name:"students",label:"Students",min:5,max:120,value:30},{type:"check",name:"board",label:"Board / teaching wall",checked:true},{type:"check",name:"teacher",label:"Teacher area",checked:true},{type:"check",name:"storage",label:"Storage",checked:true},{type:"check",name:"entrance",label:"Entrance",checked:true}],
  Garden:[{type:"number",name:"plantZones",label:"Plant zones",min:1,max:12,value:4},{type:"check",name:"lawn",label:"Lawn",checked:true},{type:"check",name:"seating",label:"Seating",checked:true},{type:"check",name:"pathways",label:"Pathways",checked:true},{type:"check",name:"water",label:"Water feature",checked:false}],
  Warehouse:[{type:"number",name:"storageZones",label:"Storage zones",min:1,max:16,value:5},{type:"check",name:"loading",label:"Loading bay",checked:true},{type:"check",name:"packing",label:"Packing area",checked:true},{type:"check",name:"office",label:"Office",checked:true},{type:"check",name:"aisle",label:"Main aisle",checked:true}]
};

function renderOptions(category){
  const fields=options[category];
  if(!fields){optionsHost.innerHTML='<p class="options-placeholder">Choose a category to see a few optional details.</p>';return;}
  const numberFields=fields.filter(field=>field.type==="number");
  const checkFields=fields.filter(field=>field.type==="check");
  const numberMarkup=numberFields.map(field=>`<label>${field.label}<input type="number" name="${field.name}" min="${field.min}" max="${field.max}" value="${field.value}" required></label>`).join("");
  const checkMarkup=checkFields.map(field=>`<label class="check-option"><input type="checkbox" name="${field.name}" value="true" ${field.checked?"checked":""}>${field.label}</label>`).join("");
  optionsHost.innerHTML=`<p class="options-title">Adjust what you want included (you can change these defaults).</p><div class="option-grid">${numberMarkup}${checkMarkup}</div>`;
}

function showMessage(text,kind="error") {message.textContent=text;message.dataset.kind=kind;message.hidden=false;}
function fixtureKind(label){
  const text=label.toLowerCase();
  if(/bed/.test(text))return "bed";
  if(/wardrobe|closet/.test(text))return "wardrobe";
  if(/bath|washroom|toilet/.test(text))return "bath";
  if(/kitchen/.test(text))return "kitchen";
  if(/tv/.test(text))return "tv";
  if(/sofa|living|lounge/.test(text))return "sofa";
  if(/study|desk|work area|cabin|reception|teacher|office/.test(text))return "desk";
  if(/dining|table|seating|guest|student desks/.test(text))return "dining";
  if(/garden|plant|lawn|water feature/.test(text))return "plant";
  if(/rack|storage|warehouse|loading|packing|aisle/.test(text))return "shelves";
  if(/parking/.test(text))return "car";
  if(/stage|dj|entertainment/.test(text))return "stage";
  if(/entrance/.test(text))return "door";
  if(/circulation|pathway/.test(text))return "path";
  return "table";
}
const roomFurnitureLayouts=[
  {bed:[5,30,43,49],wardrobe:[69,6,25,18],desk:[58,70,34,18],tv:[38,7,25,13],sofa:[54,38,32,18],path:[42,48,18,17]},
  {bed:[52,29,43,49],wardrobe:[6,6,25,18],desk:[8,70,34,18],tv:[37,7,25,13],sofa:[13,38,32,18],path:[41,48,18,17]},
  {bed:[23,5,54,38],wardrobe:[5,64,25,18],desk:[66,65,29,18],tv:[38,48,25,13],sofa:[7,43,28,17],path:[41,40,18,16]},
  {bed:[50,54,44,39],wardrobe:[5,5,25,18],desk:[58,7,35,18],tv:[37,34,25,13],sofa:[8,39,30,18],path:[39,34,17,17]},
  {bed:[5,6,44,45],wardrobe:[67,6,27,18],desk:[65,72,30,18],tv:[37,57,25,13],sofa:[10,60,30,18],path:[45,48,15,17]},
  {bed:[28,55,45,38],wardrobe:[7,8,24,18],desk:[66,8,28,18],tv:[38,38,25,13],sofa:[7,66,27,17],path:[43,39,16,14]}
];
function addPlanDetails(plan,variant,width,length){
  const topWindow=document.createElement("span");topWindow.className=`plan-window plan-window-top window-v${variant}`;topWindow.setAttribute("aria-hidden","true");
  const sideWindow=document.createElement("span");sideWindow.className=`plan-window plan-window-side window-v${variant}`;sideWindow.setAttribute("aria-hidden","true");
  const north=document.createElement("span");north.className="plan-north";north.textContent="N ↑";
  const entry=document.createElement("span");entry.className="plan-entry";entry.textContent="ENTRY";
  const horizontalDimension=document.createElement("span");horizontalDimension.className="plan-dimension plan-dimension-top";horizontalDimension.textContent=`${width} ft`;
  const verticalDimension=document.createElement("span");verticalDimension.className="plan-dimension plan-dimension-side";verticalDimension.textContent=`${length} ft`;
  plan.append(topWindow,sideWindow,north,entry,horizontalDimension,verticalDimension);
}
function makePlan(design,width,length,category,variant){
  const plan=document.createElement("div");plan.className=`floor-plan plan-variant-${variant+1}${category==="Room"?" room-plan":""}`;plan.style.setProperty("--plan-ratio",`${width}/${length}`);
  if(category==="Room"){
    const layout=roomFurnitureLayouts[variant];
    design.items.forEach(item=>{
      const kind=fixtureKind(item.label);const position=layout[kind]||layout.path;
      const furniture=document.createElement("div");furniture.className=`room-fixture fixture-${kind}`;
      furniture.style.left=`${position[0]}%`;furniture.style.top=`${position[1]}%`;furniture.style.width=`${position[2]}%`;furniture.style.height=`${position[3]}%`;
      const icon=document.createElement("span");icon.className="fixture-drawing";icon.setAttribute("aria-hidden","true");
      const label=document.createElement("span");label.className="fixture-label";label.textContent=item.label;
      furniture.append(icon,label);plan.append(furniture);
    });
  }else{
    design.items.forEach(item=>{
      const zone=document.createElement("div");zone.className=`plan-zone zone-${fixtureKind(item.label)}`;
      zone.style.left=`${item.x}%`;zone.style.top=`${item.y}%`;zone.style.width=`${item.width}%`;zone.style.height=`${item.height}%`;
      const label=document.createElement("span");label.className="zone-label";label.textContent=item.label;
      const furniture=document.createElement("span");furniture.className="zone-furniture";furniture.setAttribute("aria-hidden","true");
      zone.append(label,furniture);plan.append(zone);
    });
  }
  addPlanDetails(plan,variant,width,length);
  return plan;
}
function showDesigns(data){
  summary.textContent=`${data.category} · ${data.width} ft × ${data.length} ft · Choose the arrangement you like best.`;
  designGrid.replaceChildren();
  data.designs.forEach((design,index)=>{
    const card=document.createElement("article");card.className="design-card";
    const header=document.createElement("header");const title=document.createElement("h3");title.textContent=design.name||`Design ${index+1}`;const description=document.createElement("p");description.textContent=design.description||"A different way to arrange your spaces.";header.append(title,description);
    const wrap=document.createElement("div");wrap.className="plan-wrap";wrap.append(makePlan(design,data.width,data.length,data.category,index%6));
    const actions=document.createElement("div");actions.className="design-actions";
    const save=document.createElement("button");save.type="button";save.className="button save-design-button";save.textContent="Save design";
    const saveMessage=document.createElement("span");saveMessage.className="save-design-message";saveMessage.setAttribute("role","status");
    save.addEventListener("click",async()=>{
      if(!lastGenerationParams)return;
      save.disabled=true;save.textContent="Saving…";saveMessage.textContent="";
      try{
        const payload=new URLSearchParams(lastGenerationParams);
        payload.set("designIndex",String(index));
        payload.set("designName",design.name||`Design ${index+1}`);
        payload.set("description",design.description||"");
        if(design.items)payload.set("itemsJson",JSON.stringify(design.items));
        const response=await fetch("layout/saved",{method:"POST",body:payload,credentials:"include",headers:{Accept:"application/json"}});
        const result=await response.json();
        if(response.status===401){location.href=`login.html?category=${encodeURIComponent(data.category)}`;return;}
        if(!response.ok)throw new Error(result.error||"Could not save this design.");
        save.textContent="Saved ✓";save.classList.add("saved");
        saveMessage.innerHTML='Saved! <a href="saved-designs.html" class="view-saved-link">View in Saved designs →</a>';
      }catch(error){save.disabled=false;save.textContent="Save design";saveMessage.textContent=error.message||"Could not save this design.";}
    });
    actions.append(save,saveMessage);card.append(header,actions,wrap);designGrid.append(card);
  });
  results.hidden=false;results.scrollIntoView({behavior:"smooth",block:"start"});
}

categorySelect.addEventListener("change",()=>renderOptions(categorySelect.value));
const selectedCategory=new URLSearchParams(location.search).get("category");
if(selectedCategory&&options[selectedCategory]){categorySelect.value=selectedCategory;renderOptions(selectedCategory);}

form.addEventListener("submit",async event=>{
  event.preventDefault();message.hidden=true;results.hidden=true;
  if(!categorySelect.value){showMessage("Please choose one of the 10 categories first.");categorySelect.focus();return;}
  button.disabled=true;button.textContent="Making your six ideas…";
  try{
    lastGenerationParams=new URLSearchParams(new FormData(form));
    const response=await fetch("layout/generate",{method:"POST",body:new URLSearchParams(lastGenerationParams),credentials:"include",headers:{"Accept":"application/json"}});
    const data=await response.json();
    if(response.status===401){location.href=`login.html?category=${encodeURIComponent(categorySelect.value)}`;return;}
    if(!response.ok)throw new Error(data.error||"We couldn’t generate layouts. Check your details and try again.");
    showDesigns(data);showMessage("Your six layout ideas are ready.","success");
  }catch(error){showMessage(error.message||"Could not reach DreamHouse. Please try again.");}
  finally{button.disabled=false;button.innerHTML='Generate 6 ideas <span aria-hidden="true">→</span>';}
});

async function updateNavUser(){
  const cached=sessionStorage.getItem("dreamhouse.userName")||"";
  if(cached){document.querySelectorAll(".user-display-name").forEach(el=>el.textContent=cached);}
  try{
    const res=await fetch("auth/status",{headers:{Accept:"application/json"},cache:"no-store",credentials:"include"});
    if(res.ok){
      const data=await res.json();
      if(data.authenticated&&data.name){
        sessionStorage.setItem("dreamhouse.userName",data.name);
        document.querySelectorAll(".user-display-name").forEach(el=>el.textContent=data.name);
      }
    }
  }catch(err){}
}
updateNavUser();

