package com.library.sdl.UserSeat;

public class SeatStatusDTO {


        private int seatName;

        private boolean isRegistered;

        // Constructor
        public SeatStatusDTO(int seatName, boolean isRegistered) {
            this.seatName = seatName;

            this.isRegistered = isRegistered;
        }


    public int getSeatName() {
        return seatName;
    }

    public void setSeatName(int seatName) {
        this.seatName = seatName;
    }

    public boolean isRegistered() {
            return isRegistered;
        }

        public void setIsRegistered(boolean isRegistered) {
            this.isRegistered = isRegistered;
        }
    }


