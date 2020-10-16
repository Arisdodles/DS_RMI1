package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.List;

import rental.CarType;
import rental.ICarRentalCompany;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;

public class Client extends AbstractTestBooking {

	/********
	 * MAIN *
	 ********/

	private final static int LOCAL = 0;
	private final static int REMOTE = 1;
	ICarRentalCompany crc;
	/**
	 * The `main` method is used to launch the client application and run the test
	 * script.
	 */
	public static void main(String[] args) throws Exception {
		// The first argument passed to the `main` method (if present)
		// indicates whether the application is run on the remote setup or not.
		int localOrRemote = (args.length == 1 && args[0].equals("REMOTE")) ? REMOTE : LOCAL;

		System.setSecurityManager(null);
		
		String carRentalCompanyName = "Hertz";

		// An example reservation scenario on car rental company 'Hertz' would be...
		Client client = new Client("simpleTrips", carRentalCompanyName, localOrRemote);
		client.run();
	}

	/***************
	 * CONSTRUCTOR 
	 * @throws RemoteException 
	 * @throws NotBoundException *
	 ***************/

	public Client(String scriptFile, String carRentalCompanyName, int localOrRemote) throws RemoteException, NotBoundException {
		super(scriptFile);
		
		Registry registry;
		
		if(localOrRemote == LOCAL) {
			registry = LocateRegistry.getRegistry("localhost", 12345);
		} else {
			registry = LocateRegistry.getRegistry("10.10.10.54", 12345);
		}
		
		crc = (ICarRentalCompany)registry.lookup(carRentalCompanyName);
	}

	/**
	 * Check which car types are available in the given period (across all companies
	 * and regions) and print this list of car types.
	 *
	 * @param start start time of the period
	 * @param end   end time of the period
	 * @throws Exception if things go wrong, throw exception
	 */
	@Override
	protected void checkForAvailableCarTypes(Date start, Date end) throws Exception {

		Set<CarType> avaCarTypes = crc.getAvailableCarTypes(start, end);
		for(CarType ct: avaCarTypes) {
			System.out.println(ct);
		}
	}

	/**
	 * Retrieve a quote for a given car type (tentative reservation).
	 * 
	 * @param clientName name of the client
	 * @param start      start time for the quote
	 * @param end        end time for the quote
	 * @param carType    type of car to be reserved
	 * @param region     region in which car must be available
	 * @return the newly created quote
	 * 
	 * @throws Exception if things go wrong, throw exception
	 */
	@Override
	protected Quote createQuote(String clientName, Date start, Date end, String carType, String region)
			throws Exception {

		ReservationConstraints reservationConstraints = new ReservationConstraints(start, end, carType, region);
		
		Quote quote = crc.createQuote(reservationConstraints, clientName);
		System.out.println(quote);
		return quote;

	}

	/**
	 * Confirm the given quote to receive a final reservation of a car.
	 * 
	 * @param quote the quote to be confirmed
	 * @return the final reservation of a car
	 * 
	 * @throws Exception if things go wrong, throw exception
	 */
	@Override
	protected Reservation confirmQuote(Quote quote) throws Exception {

		Reservation reservation = crc.confirmQuote(quote);
		System.out.println("Reservation is confirmed.");
		return reservation;
	}

	/**
	 * Get all reservations made by the given client.
	 *
	 * @param clientName name of the client
	 * @return the list of reservations of the given client
	 * 
	 * @throws Exception if things go wrong, throw exception
	 */
	@Override
	protected List<Reservation> getReservationsByRenter(String clientName) throws Exception {

		List<Reservation> renterReservations = new LinkedList<Reservation>();
		List<Reservation> allReservations = crc.getReservations();
		for(Reservation r: allReservations) {
			if(r.getCarRenter().equals(clientName)) {
				renterReservations.add(r);
				System.out.println(r);
			}
		}
		return renterReservations;
	}

	/**
	 * Get the number of reservations for a particular car type.
	 * 
	 * @param carType name of the car type
	 * @return number of reservations for the given car type
	 * 
	 * @throws Exception if things go wrong, throw exception
	 */
	@Override
	protected int getNumberOfReservationsForCarType(String carType) throws Exception {

		List<Reservation> allReservations = crc.getReservations();
		int count = 0;
		for(Reservation r: allReservations) {
			if(r.getCarType().equals(carType)) {
				count++;
			}
		}
		System.out.println(String.format("Number of reservations for cars of the type %s: %d", carType, count));
		return count;
	}
}