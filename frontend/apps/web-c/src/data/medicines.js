import productColdMedicine from "@shared/assets/product-cold-medicine.svg";
import productPainRelief from "@shared/assets/product-pain-relief.svg";
import productStomachMedicine from "@shared/assets/product-stomach-medicine.svg";
import productCoughSyrup from "@shared/assets/product-cough-syrup.svg";

export const medicines = [
  {
    id: 1,
    name: "感冒灵颗粒",
    tag: "感冒发热",
    category: "家庭常备",
    price: 26,
    marketPrice: 39,
    desc: "适合常见感冒引起的发热、头痛与鼻塞不适。",
    image: productColdMedicine,
    flashSale: true,
    recommend: true
  },
  {
    id: 2,
    name: "布洛芬缓释胶囊",
    tag: "止痛退热",
    category: "家庭常备",
    price: 32,
    marketPrice: 46,
    desc: "用于缓解发热、头痛、牙痛等常见疼痛症状。",
    image: productPainRelief,
    flashSale: true,
    recommend: true
  },
  {
    id: 3,
    name: "铝碳酸镁咀嚼片",
    tag: "胃部不适",
    category: "肠胃用药",
    price: 29,
    marketPrice: 41,
    desc: "适合反酸、胃胀与轻度胃部不适的日常缓解。",
    image: productStomachMedicine,
    flashSale: false,
    recommend: true
  },
  {
    id: 4,
    name: "川贝枇杷糖浆",
    tag: "咳嗽护理",
    category: "呼吸护理",
    price: 24,
    marketPrice: 36,
    desc: "适合轻度咳嗽、咽喉刺激等场景的基础护理。",
    image: productCoughSyrup,
    flashSale: true,
    recommend: true
  },
  {
    id: 5,
    name: "板蓝根颗粒",
    tag: "咽喉不适",
    category: "感冒发热",
    price: 18,
    marketPrice: 28,
    desc: "适合季节交替时的咽喉不适与日常防护补充。",
    image: productColdMedicine,
    flashSale: false,
    recommend: true
  },
  {
    id: 6,
    name: "小儿氨酚黄那敏颗粒",
    tag: "儿童常备",
    category: "家庭常备",
    price: 22,
    marketPrice: 32,
    desc: "适合儿童感冒时的发热、流涕等常见症状护理。",
    image: productPainRelief,
    flashSale: false,
    recommend: false
  },
  {
    id: 7,
    name: "蒙脱石散",
    tag: "肠胃护理",
    category: "肠胃用药",
    price: 19,
    marketPrice: 27,
    desc: "适合轻度腹泻与肠胃敏感时的基础护理。",
    image: productStomachMedicine,
    flashSale: true,
    recommend: true
  },
  {
    id: 8,
    name: "复方鲜竹沥液",
    tag: "止咳化痰",
    category: "呼吸护理",
    price: 21,
    marketPrice: 33,
    desc: "适合咳痰偏多、咽喉黏滞时的日常缓解护理。",
    image: productCoughSyrup,
    flashSale: false,
    recommend: true
  },
  {
    id: 9,
    name: "奥美拉唑肠溶胶囊",
    tag: "胃酸反流",
    category: "肠胃用药",
    price: 31,
    marketPrice: 44,
    desc: "适合反酸、烧心等胃酸分泌偏多场景的基础用药。",
    image: productStomachMedicine,
    flashSale: true,
    recommend: false
  },
  {
    id: 10,
    name: "氯雷他定片",
    tag: "过敏常备",
    category: "过敏护理",
    price: 27,
    marketPrice: 38,
    desc: "适合季节性过敏、鼻痒流涕等常见过敏场景。",
    image: productPainRelief,
    flashSale: false,
    recommend: true
  },
  {
    id: 11,
    name: "双黄连口服液",
    tag: "清热护理",
    category: "感冒发热",
    price: 23,
    marketPrice: 34,
    desc: "适合换季时期的清热护理与咽喉不适缓解。",
    image: productColdMedicine,
    flashSale: true,
    recommend: false
  },
  {
    id: 12,
    name: "健胃消食片",
    tag: "饭后胀满",
    category: "肠胃用药",
    price: 17,
    marketPrice: 25,
    desc: "适合餐后积食、轻度腹胀等日常消化护理场景。",
    image: productStomachMedicine,
    flashSale: false,
    recommend: true
  }
];

export function findMedicineById(id) {
  return medicines.find((item) => String(item.id) === String(id));
}
