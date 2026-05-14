<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleClick" type="card" v-if="shopType!==911">
      <el-tab-pane label="按商品展示" name="goodsList" >
       <goods-list :shopType="shopType" :refresh="refresh" :targetShopId="targetShopId"></goods-list>
      </el-tab-pane>
      <el-tab-pane label="按规格展示" name="skuList" lazy>
        <goods-sku :shopType="shopType"></goods-sku>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import GoodsList from "@/views/shop/goods/goods_list.vue";
import GoodsSku from "@/views/shop/goods/goods_sku.vue";

export default {
  name: "ShopGoodsIndex",
  components:{
    GoodsList,GoodsSku
  },
  props: {
    shopType: {
      type: Number,  // 根据你的数据类型选择合适的类型
      required: false,
    },
    refresh: {
      type: String,
      required: false,
      default: ''
    },
    targetShopId: {
      type: String,
      required: false,
      default: ''
    }
  },
  data() {
    return {
      activeName: 'goodsList',
      typeList: [],
    };
  },
  created() {

  },
  mounted() {
    console.log('=====shopType',this.shopType)
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event);
    }
  }
};
</script>
