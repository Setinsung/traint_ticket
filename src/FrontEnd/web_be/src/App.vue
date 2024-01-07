<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';
import dayjs from 'dayjs';
const startPo = ref<number | undefined>(undefined);
const endPo = ref<number | undefined>(undefined);
const startDate = ref<Date>();

interface IStation {
  id: number;
  name: string;
  cityId: number;
}
interface ISeat {
  id: number;
  number: string;
  leftCount: number;
  showEnd: string;
  showStart: string;
  startStation?: string;
  endStation?: string;
  duringTime: string;
}
interface IRes {
  code: number;
  data: any;
  msg?: string;
  ret: boolean;
}
interface IGetStationListRes extends IRes {
  data: IStation[];
}
interface ISearchSeatRes extends IRes {
  data: ISeat[];
}
interface ISearchSeatReq {
  date: string;
  fromStationId?: number;
  toStationId?: number;
}

const stationList = ref<IStation[]>([]);
const seatList = ref<ISeat[]>([]);

const getStationList = async () => {
  const res = await axios.get<IGetStationListRes>("/api/station/list");
  console.log('res.data', res.data);
  stationList.value = res.data.data;
};
const findNameById = (id: number) =>  {
  const foundData = stationList.value.find(item => item.id === id);
  return foundData ? foundData.name : undefined;
}

const searchSeat = async (data: ISearchSeatReq) => {
  const res = await axios.post<ISearchSeatRes>(`/api/station/search?date=${data.date}&fromStationId=${data.fromStationId}&toStationId=${data.toStationId}`);
  seatList.value = res.data.data;
  seatList.value.forEach(item => {
    item.startStation = findNameById(startPo.value!);
    item.endStation = findNameById(endPo.value!);
  });
};

const formatDate = (oldDate: Date) => {
  const formattedDate = dayjs(oldDate).format('YYYYMMDD');
  return formattedDate;
};


const onTableRowClassName = ({ _, rowIndex }: { _: number, rowIndex: number; }) => {
  if (rowIndex % 2 == 0) {
    return 'statistics-warning-row';
  } else {
    return 'statistics-commom-row';
  }
};

const onSearch = async () => {
  const formattedStartDate = formatDate(startDate.value!);
  console.log('formattedStartDate', formattedStartDate);
  await searchSeat({ date: formattedStartDate, fromStationId: startPo.value, toStationId: endPo.value });
  // console.log('obj',{ date: formattedStartDate, fromStationId: startPo.value, toStationId: endPo.value });
};



// 初始化
getStationList();
</script>

<template>
  <div>
    <div class="logo-header">
      <img class="logo-img" src="./assets/logo.jpg" alt="">
      <div class="header-list">
        <li>首页</li>
        <li class="header-list-selected">车票</li>
        <li>团购服务</li>
        <li>会员服务</li>
        <li>站车服务</li>
        <li>商旅服务</li>
        <li>出行指南</li>
        <li>信息查询</li>
      </div>
    </div>
    <div class="search-content">
      <el-card class="search-content-card">
        <div class="search-inputs">
          <div class="search-item">
            <span class="item-label">出发地</span>
            <el-select class="search-item-input" v-model="startPo" size="default">
              <el-option v-for="item in stationList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>

          <div class="search-item">
            <span class="item-label">目的地</span>
            <el-select class="search-item-input" v-model="endPo" size="default">
              <el-option v-for="item in stationList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>

          <div class="search-item search-date">
            <span class="item-label">出发日</span>
            <el-date-picker class="search-item-input" size="default" v-model="startDate" type="date" />
          </div>
          <el-button class="search-btn" @click="onSearch">查询</el-button>
        </div>
      </el-card>
      <el-table :data="seatList" border class="search-content-table"
        :header-cell-style="{ background: '#2689ca', color: '#fff', font: '12px Tahoma, 宋体', fontWeight: 700 }"
        :row-class-name="onTableRowClassName">
        <el-table-column align="center" prop="number" label="车次" />
        <el-table-column align="center" prop="startStation" label="出发站" width="100" />
        <el-table-column align="center" prop="endStation" label="到达站" width="100" />
        <el-table-column align="center" prop="showStart" label="出发时间" width="100" />
        <el-table-column align="center" prop="showEnd" label="到达时间" width="100" />
        <el-table-column align="center" prop="duringTime" label="历时" width="100" />
        <el-table-column align="center" prop="leftCount" label="座位" width="100" />
        <el-table-column align="center" label="备注" width="108">
            <el-button class="book-btn" size="small">预订</el-button>
        </el-table-column>
      </el-table>
    </div>


  </div>
</template>

<style scoped>
.logo-header {}

.logo-img {
  width: 225px;
  margin-left: 30px;
  margin-top: 5px;
}

.header-list {
  background-color: #3b99fc;
  display: flex;
  padding: 0 35px;
  color: #fff;
}

.header-list li {
  font-size: 14px;
  height: 40px;
  width: 150px;
  text-align: center;
  line-height: 40px;
}

.header-list li:hover {
  background-color: #2676e3;
}

.header-list-selected {
  background-color: #2676e3;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-content {
  margin-left: 42px;
  margin-right: 42px;
  margin-top: 10px;
}

.search-content-card {
  background-color: #eef1f8;
  border: 1px solid #298cce;
}

.search-inputs {
  width: 100%;
  display: flex;
}

.search-item {
  font: 12px Tahoma, 宋体;
  display: flex;
  align-items: center;
  margin-right: 60px;
}

.item-label {
  margin-right: 10px;
}

.search-item-input {
  width: 120px;
}

.search-date {
  margin-right: 300px;
}

.search-btn {
  background-color: #f79209;
  color: #fff;
  width: 92px;
  font: 12px Tahoma, 宋体;
}

.search-content-table {
  margin-top: 10px;
  border: 1px solid #298cce;
}

.book-btn {
  width: 72px;
  height: 30px;
  font: 12px Tahoma, 宋体;
  color: #fff;
  background-color: #208fff;
  border-radius: 4px;
}
</style>
