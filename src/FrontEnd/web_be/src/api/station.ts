import axios from "axios";


export const getStationList = async () => {
  const res = await axios.get("/api/station/list");
  return res.data;
};