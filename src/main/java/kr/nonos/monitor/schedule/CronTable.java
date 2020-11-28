package kr.nonos.monitor.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.nonos.monitor.machine.entity.Machine;
import kr.nonos.monitor.machine.entity.MachineStatusFailLog;
import kr.nonos.monitor.machine.entity.MachineStatusFront;
import kr.nonos.monitor.machine.entity.MachineStatusLog;
import kr.nonos.monitor.machine.entity.MachineStatusWatchdogLog;
import kr.nonos.monitor.machine.repository.MachineRepository;
import kr.nonos.monitor.machine.repository.MachineStatusFailLogRepository;
import kr.nonos.monitor.machine.repository.MachineStatusFrontRepository;
import kr.nonos.monitor.machine.repository.MachineStatusLogRepository;
import kr.nonos.monitor.machine.repository.MachineStatusWatchdogLogRepository;

@Component
public class CronTable {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private MachineRepository machineRepository;

	@Autowired
	private MachineStatusFrontRepository machineStatusFrontRepository;

	@Autowired
	private MachineStatusLogRepository machineStatusLogRepository;

	@Autowired
	private MachineStatusFailLogRepository machineStatusFailLogRepository;

	@Autowired
	private MachineStatusWatchdogLogRepository machineStatusWatchdogLogRepository;

	@Value("${sql.query.rename.machine_status_log}")
	private String queryRenameMachineStatusLog;

	@Value("${sql.query.create.machine_status_log}")
	private String queryCreateMachineStatusLog;

	private int countStatusCron = 0;
	private int countLogCron = 0;

	final private int COUNT_STATUS = 1;
	final private int COUNT_LOG = 10;

	@Scheduled(cron = "0/30 * * * * *")
	public void getMachineStatusWatchDogLogCron() throws Exception {
		countStatusCron++;
		countLogCron++;

		List<Machine> machineList = machineRepository.findAll();

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();

		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();

		try {
			httpclient.start();

			final CountDownLatch latch = new CountDownLatch(machineList.size());

			for (Machine machine : machineList) {
				httpclient.execute(new HttpGet("http://" + machine.getIp() + ":" + machine.getPort() + "/"), new FutureCallback<HttpResponse>() {
					private int machineId = machine.getId();

					@Override
					public void completed(final HttpResponse response) {
						latch.countDown();

						StringBuilder sb = new StringBuilder();

						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);

							String line = null;

							while ((line = reader.readLine()) != null) {
								sb.append(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

						JSONParser jsonParser = new JSONParser();

						JSONObject machineStatusObject;

						try {
							String jsonString = sb.toString();

							// incorrect share check
							Pattern pattern = Pattern.compile("GPU #[0-9] got incorrect share.");

							Matcher matcher = pattern.matcher(jsonString);

							int startPosition = 0;

							while (matcher.find(startPosition)) {
								startPosition = matcher.start() + 1;

								MachineStatusWatchdogLog machineStatusWatchdogLog = new MachineStatusWatchdogLog();

								SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

								Calendar cal = Calendar.getInstance();

								machineStatusWatchdogLog.setMachineId(machineId);
								machineStatusWatchdogLog.setWatchdogStatus("1");
								machineStatusWatchdogLog.setUpdateDatetime(fr.format(cal.getTime()));
								machineStatusWatchdogLog.setGpuNumber(jsonString.substring(matcher.start() + 5, matcher.start() + 6));

								machineStatusWatchdogLogRepository.save(machineStatusWatchdogLog);
							}

							// hangs in OpenCL call check
							pattern = Pattern.compile("GPU #[0-9] hangs in OpenCL call, exit");

							matcher = pattern.matcher(jsonString);

							startPosition = 0;

							while (matcher.find(startPosition)) {
								MachineStatusWatchdogLog machineStatusWatchdogLog = new MachineStatusWatchdogLog();

								SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

								Calendar cal = Calendar.getInstance();

								machineStatusWatchdogLog.setMachineId(machineId);
								machineStatusWatchdogLog.setWatchdogStatus("2");
								machineStatusWatchdogLog.setUpdateDatetime(fr.format(cal.getTime()));
								machineStatusWatchdogLog.setGpuNumber(jsonString.substring(matcher.start() + 5, matcher.start() + 6));

								machineStatusWatchdogLogRepository.save(machineStatusWatchdogLog);
							}

							// restart miner check
							if (jsonString.indexOf("GPU error, you need to restart miner") != -1) {
								MachineStatusWatchdogLog machineStatusWatchdogLog = new MachineStatusWatchdogLog();

								SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

								Calendar cal = Calendar.getInstance();

								machineStatusWatchdogLog.setMachineId(machineId);
								machineStatusWatchdogLog.setWatchdogStatus("3");
								machineStatusWatchdogLog.setUpdateDatetime(fr.format(cal.getTime()));

								machineStatusWatchdogLogRepository.save(machineStatusWatchdogLog);
							}

							// save status with COUNT_STATUS
							if (countStatusCron >= COUNT_STATUS) {
								if (latch.getCount() <= 0) {
									countStatusCron = 0;
								}

								try {
									jsonString = sb.toString();

									machineStatusObject = (JSONObject) jsonParser.parse(jsonString.substring(jsonString.indexOf("{"), jsonString.indexOf("}") + 1));

									JSONArray machineStatusArray = (JSONArray) machineStatusObject.get("result");

									MachineStatusFront machineStatusFront = machineStatusFrontRepository.findOne(machineId);
	
									if (machineStatusFront != null && machineStatusFront.getRunningTime() == null) {
										MachineStatusFailLog machineStatusFailLog = new MachineStatusFailLog();
	
										machineStatusFailLog.setMachineId(machineId);
										machineStatusFailLog.setMachineStatus("1");
	
										SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
										Calendar cal = Calendar.getInstance();
	
										machineStatusFailLog.setUpdateDatetime(fr.format(cal.getTime()));
	
										machineStatusFailLogRepository.save(machineStatusFailLog);
									}
	
									machineStatusFront = new MachineStatusFront();
	
									machineStatusFront.setMachineId(machineId);
									machineStatusFront.setVersion((String) machineStatusArray.get(0));
									machineStatusFront.setRunningTime((String) machineStatusArray.get(1));
									machineStatusFront.setEthereumStats((String) machineStatusArray.get(2));
									machineStatusFront.setEthereumSpeed((String) machineStatusArray.get(3));
									machineStatusFront.setDecredStats((String) machineStatusArray.get(4));
									machineStatusFront.setDecredSpeed((String) machineStatusArray.get(5));
									machineStatusFront.setGpuTemperature((String) machineStatusArray.get(6));
									machineStatusFront.setPool((String) machineStatusArray.get(7));
									machineStatusFront.setQuestion((String) machineStatusArray.get(8));
	
									SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
									Calendar cal = Calendar.getInstance();
	
									machineStatusFront.setUpdateDatetime(fr.format(cal.getTime()));
	
									machineStatusFrontRepository.save(machineStatusFront);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							// save log with COUNT_LOG
							if (countLogCron >= COUNT_LOG) {
								if (latch.getCount() <= 0) {
									countLogCron = 0;
								}

								try {
									jsonString = sb.toString();

									machineStatusObject = (JSONObject) jsonParser.parse(jsonString.substring(jsonString.indexOf("{"), jsonString.indexOf("}") + 1));

									JSONArray machineStatusArray = (JSONArray) machineStatusObject.get("result");

									SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

									Calendar cal = Calendar.getInstance();

									MachineStatusLog machineStatusLog = new MachineStatusLog();

									machineStatusLog.setMachineId(machineId);
									machineStatusLog.setVersion((String) machineStatusArray.get(0));
									machineStatusLog.setRunningTime((String) machineStatusArray.get(1));
									machineStatusLog.setEthereumStats((String) machineStatusArray.get(2));
									machineStatusLog.setEthereumSpeed((String) machineStatusArray.get(3));
									machineStatusLog.setDecredStats((String) machineStatusArray.get(4));
									machineStatusLog.setDecredSpeed((String) machineStatusArray.get(5));
									machineStatusLog.setGpuTemperature((String) machineStatusArray.get(6));
									machineStatusLog.setPool((String) machineStatusArray.get(7));
									machineStatusLog.setQuestion((String) machineStatusArray.get(8));

									cal = Calendar.getInstance();

									machineStatusLog.setUpdateDatetime(fr.format(cal.getTime()));

									machineStatusLogRepository.save(machineStatusLog);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void failed(final Exception ex) {
						latch.countDown();

						if (countStatusCron >= COUNT_STATUS) {
							if (latch.getCount() <= 0) {
								countStatusCron = 0;
							}

							MachineStatusFront machineStatusFront = machineStatusFrontRepository.findOne(machineId);

							if (machineStatusFront != null && machineStatusFront.getRunningTime() != null) {
								MachineStatusFailLog machineStatusFailLog = new MachineStatusFailLog();

								machineStatusFailLog.setMachineId(machineId);
								machineStatusFailLog.setMachineStatus("2");

								SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

								Calendar cal = Calendar.getInstance();

								machineStatusFailLog.setUpdateDatetime(fr.format(cal.getTime()));

								machineStatusFailLogRepository.save(machineStatusFailLog);
							}

							Calendar cal = Calendar.getInstance();

							SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

							machineStatusFront = new MachineStatusFront();

							machineStatusFront.setMachineId(machineId);
							machineStatusFront.setUpdateDatetime(fr.format(cal.getTime()));

							machineStatusFrontRepository.save(machineStatusFront);
						}

						if (countStatusCron >= COUNT_LOG) {
							if (latch.getCount() <= 0) {
								countLogCron = 0;
							}

							Calendar cal = Calendar.getInstance();

							SimpleDateFormat fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

							MachineStatusLog machineStatusLog = new MachineStatusLog();

							machineStatusLog.setMachineId(machine.getId());
							machineStatusLog.setUpdateDatetime(fr.format(cal.getTime()));

							machineStatusLogRepository.save(machineStatusLog);

							System.out.println(machine.getIp() + "->" + ex);
						}
					}

					@Override
					public void cancelled() {
						latch.countDown();
					}
				});
			}

			latch.await();
		} finally {
			httpclient.close();
		}
	}

	@Scheduled(cron = "0 5 0 1 * *")
	public void renameTableMachineStatusLog() throws Exception {
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.MONTH, -1);

		SimpleDateFormat fr = new SimpleDateFormat("yyyyMM");

		jdbcTemplate.execute(queryRenameMachineStatusLog + fr.format(cal.getTime()).toString());

		jdbcTemplate.execute(queryCreateMachineStatusLog);
	}
}
